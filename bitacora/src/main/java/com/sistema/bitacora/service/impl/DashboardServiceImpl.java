package com.sistema.bitacora.service.impl;

import com.sistema.bitacora.dto.caso.CasoResponse;
import com.sistema.bitacora.dto.caso.DashboardResponse;
import com.sistema.bitacora.dto.caso.DashboardService;
import com.sistema.bitacora.entity.Caso;
import com.sistema.bitacora.mapper.CasoMapper;
import com.sistema.bitacora.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final EntityManager entityManager;
    private final CasoMapper casoMapper;
    private final UsuarioRepository usuarioRepository;

    @Override
    public DashboardResponse obtenerEstadisticas() {
        return obtenerEstadisticas(null, null, "", null, null);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardResponse obtenerEstadisticas(Long servicioId, Long estadoId, String termino,
                                                  LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        Filtros filtros = new Filtros(servicioId, estadoId, termino, fechaDesde, fechaHasta, serviciosPermitidosIds());
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime inicioTendencia = ahora.withDayOfMonth(1).minusMonths(5).toLocalDate().atStartOfDay();

        return DashboardResponse.builder()
                .totalCasos(contar(filtros, null))
                .casosActivos(contar(filtros, (cb, root) -> cb.not(root.get("estado").get("nombre").in("SOLUCIONADO", "CERRADO"))))
                .casosEscalados(contar(filtros, (cb, root) -> cb.equal(root.get("estado").get("nombre"), "ESCALADO")))
                .ansVencidos(contar(filtros, (cb, root) -> cb.and(
                        cb.not(root.get("estado").get("nombre").in("SOLUCIONADO", "CERRADO")),
                        cb.isNotNull(root.get("fechaVencimiento")),
                        cb.lessThan(root.get("fechaVencimiento"), ahora))))
                .casosPorEstado(agruparPor(filtros, root -> root.get("estado").get("nombre")))
                .casosPorServicio(agruparPor(filtros, root -> root.get("servicio").get("nombre")))
                .casosPorSede(agruparPorSede(filtros))
                .casosPorMes(agruparPorMes(filtros, inicioTendencia))
                .casosRecientes(obtenerRecientes(filtros))
                .build();
    }

    private long contar(Filtros filtros, FiltroExtra filtroExtra) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Caso> root = query.from(Caso.class);
        List<Predicate> predicates = crearPredicados(cb, root, filtros);
        if (filtroExtra != null) predicates.add(filtroExtra.crear(cb, root));
        query.select(cb.count(root)).where(predicates.toArray(Predicate[]::new));
        return entityManager.createQuery(query).getSingleResult();
    }

    private Map<String, Long> agruparPor(Filtros filtros, Function<Root<Caso>, Path<String>> campo) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<Caso> root = query.from(Caso.class);
        Path<String> nombre = campo.apply(root);
        Expression<Long> total = cb.count(root);
        query.multiselect(nombre, total)
                .where(crearPredicados(cb, root, filtros).toArray(Predicate[]::new))
                .groupBy(nombre)
                .orderBy(cb.desc(total));

        Map<String, Long> resultados = new LinkedHashMap<>();
        for (Object[] fila : entityManager.createQuery(query).getResultList()) {
            resultados.put((String) fila[0], ((Number) fila[1]).longValue());
        }
        return resultados;
    }

    private Map<String, Long> agruparPorSede(Filtros filtros) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<Caso> root = query.from(Caso.class);
        Path<String> sban = root.get("sedePrincipal").get("sban");
        Path<String> nombre = root.get("sedePrincipal").get("nombreSede");
        Expression<Long> total = cb.count(root);
        query.multiselect(sban, nombre, total)
                .where(crearPredicados(cb, root, filtros).toArray(Predicate[]::new))
                .groupBy(sban, nombre)
                .orderBy(cb.desc(total));

        Map<String, Long> resultados = new LinkedHashMap<>();
        entityManager.createQuery(query).setMaxResults(8).getResultList().forEach(fila -> {
            String clave = String.valueOf(fila[0]) + " - " + String.valueOf(fila[1]);
            resultados.put(clave, ((Number) fila[2]).longValue());
        });
        return resultados;
    }

    private Map<String, Long> agruparPorMes(Filtros filtros, LocalDateTime inicioTendencia) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<Caso> root = query.from(Caso.class);
        Expression<Integer> anio = cb.function("YEAR", Integer.class, root.get("fechaSolicitud"));
        Expression<Integer> mes = cb.function("MONTH", Integer.class, root.get("fechaSolicitud"));
        Expression<Long> total = cb.count(root);
        List<Predicate> predicates = crearPredicados(cb, root, filtros);
        predicates.add(cb.greaterThanOrEqualTo(root.get("fechaSolicitud"), inicioTendencia));
        query.multiselect(anio.alias("anio"), mes.alias("mes"), total.alias("total"))
                .where(predicates.toArray(Predicate[]::new))
                .groupBy(anio, mes)
                .orderBy(cb.asc(anio), cb.asc(mes));

        Map<String, Long> resultados = new LinkedHashMap<>();
        for (Tuple fila : entityManager.createQuery(query).getResultList()) {
            String clave = "%04d-%02d".formatted(fila.get("anio", Integer.class), fila.get("mes", Integer.class));
            resultados.put(clave, fila.get("total", Long.class));
        }
        return resultados;
    }

    private List<CasoResponse> obtenerRecientes(Filtros filtros) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Caso> query = cb.createQuery(Caso.class);
        Root<Caso> root = query.from(Caso.class);
        query.select(root)
                .where(crearPredicados(cb, root, filtros).toArray(Predicate[]::new))
                .orderBy(cb.desc(root.get("fechaSolicitud")));
        return entityManager.createQuery(query).setMaxResults(5).getResultList()
                .stream().map(casoMapper::toResponse).toList();
    }

    private List<Predicate> crearPredicados(CriteriaBuilder cb, Root<Caso> root, Filtros filtros) {
        List<Predicate> predicates = new ArrayList<>();
        if (filtros.servicioIdsPermitidos() != null) {
            predicates.add(root.get("servicio").get("id").in(filtros.servicioIdsPermitidos()));
        }
        if (filtros.servicioId() != null) {
            predicates.add(cb.equal(root.get("servicio").get("id"), filtros.servicioId()));
        }
        if (filtros.estadoId() != null) {
            predicates.add(cb.equal(root.get("estado").get("id"), filtros.estadoId()));
        }
        if (filtros.fechaDesde() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("fechaSolicitud"), filtros.fechaDesde()));
        }
        if (filtros.fechaHasta() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("fechaSolicitud"), filtros.fechaHasta()));
        }
        if (filtros.termino() != null && !filtros.termino().isBlank()) {
            String patron = "%" + filtros.termino().trim().toLowerCase() + "%";
            predicates.add(cb.or(
                    cb.like(cb.lower(root.get("numeroCasoInt")), patron),
                    cb.like(cb.lower(root.get("numeroCasoESolution")), patron),
                    cb.like(cb.lower(root.get("descripcionFalla")), patron),
                    cb.like(cb.lower(root.get("sedePrincipal").get("sban")), patron),
                    cb.like(cb.lower(root.get("sedePrincipal").get("nombreSede")), patron)));
        }
        return predicates;
    }

    private List<Long> serviciosPermitidosIds() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) return List.of();
        var usuario = usuarioRepository.findByCorreo(authentication.getName()).orElse(null);
        if (usuario == null) return List.of();
        if ("ADMIN".equalsIgnoreCase(usuario.getRol().getNombre())) return null;
        return usuario.getServiciosPermitidos().stream().map(servicio -> servicio.getId()).toList();
    }

    private record Filtros(Long servicioId, Long estadoId, String termino,
                           LocalDateTime fechaDesde, LocalDateTime fechaHasta, List<Long> servicioIdsPermitidos) {}

    @FunctionalInterface
    private interface FiltroExtra {
        Predicate crear(CriteriaBuilder cb, Root<Caso> root);
    }
}
