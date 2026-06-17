package com.servify.usuarios.application.service;

import com.servify.publicaciones.application.port.out.PublicacionServicioRepositoryPort;
import com.servify.publicaciones.domain.model.PublicacionServicio;
import com.servify.shared.domain.valueobject.Ubicacion;
import com.servify.solicitudes.application.port.out.CalificacionRepositoryPort;
import com.servify.solicitudes.domain.model.Calificacion;
import com.servify.usuarios.application.dto.PrestadorPublicoResult;
import com.servify.usuarios.application.port.in.BuscarPrestadoresPublicosUseCase;
import com.servify.usuarios.application.port.out.UsuarioRepositoryPort;
import com.servify.usuarios.domain.enumtype.EstadoUsuario;
import com.servify.usuarios.domain.model.PerfilUsuario;
import com.servify.usuarios.domain.model.Usuario;
import com.servify.usuarios.domain.valueobject.NombreCompleto;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class BuscarPrestadoresPublicosService implements BuscarPrestadoresPublicosUseCase {

    private static final int LIMITE_RESULTADOS = 30;

    private final UsuarioRepositoryPort usuarioRepositoryPort;
    private final PublicacionServicioRepositoryPort publicacionServicioRepositoryPort;
    private final CalificacionRepositoryPort calificacionRepositoryPort;

    public BuscarPrestadoresPublicosService(
            UsuarioRepositoryPort usuarioRepositoryPort,
            PublicacionServicioRepositoryPort publicacionServicioRepositoryPort,
            CalificacionRepositoryPort calificacionRepositoryPort
    ) {
        this.usuarioRepositoryPort = usuarioRepositoryPort;
        this.publicacionServicioRepositoryPort = publicacionServicioRepositoryPort;
        this.calificacionRepositoryPort = calificacionRepositoryPort;
    }

    @Override
    public List<PrestadorPublicoResult> buscarPorNombreUsuario(String nombreUsuario) {
        String filtro = normalizarFiltro(nombreUsuario);
        if (filtro.isBlank()) {
            return List.of();
        }
        Map<UUID, List<PublicacionServicio>> publicacionesPorPrestador = publicacionServicioRepositoryPort.buscarActivas()
                .stream()
                .filter(publicacion -> publicacion.getUsuarioId() != null)
                .collect(Collectors.groupingBy(PublicacionServicio::getUsuarioId));

        return usuarioRepositoryPort.listarPorEstado(EstadoUsuario.ACTIVO)
                .stream()
                .filter(usuario -> publicacionesPorPrestador.containsKey(usuario.getId()))
                .filter(usuario -> coincideNombreUsuario(usuario, filtro))
                .sorted(Comparator.comparing(this::ordenNombreUsuario))
                .limit(LIMITE_RESULTADOS)
                .map(usuario -> construirResultado(usuario, publicacionesPorPrestador.get(usuario.getId())))
                .toList();
    }

    @Override
    public Optional<PrestadorPublicoResult> obtenerPorUsuarioId(UUID usuarioId) {
        if (usuarioId == null) {
            return Optional.empty();
        }
        return usuarioRepositoryPort.buscarPorId(usuarioId)
                .filter(usuario -> usuario.getEstado() == EstadoUsuario.ACTIVO)
                .map(usuario -> {
                    List<PublicacionServicio> publicaciones = publicacionServicioRepositoryPort.buscarActivas()
                            .stream()
                            .filter(publicacion -> usuarioId.equals(publicacion.getUsuarioId()))
                            .toList();
                    return construirResultado(usuario, publicaciones);
                });
    }

    private PrestadorPublicoResult construirResultado(Usuario usuario, List<PublicacionServicio> publicaciones) {
        PerfilUsuario perfil = usuario.getPerfil();
        NombreCompleto nombreCompleto = perfil != null ? perfil.getNombreCompleto() : null;
        List<Calificacion> calificaciones = calificacionRepositoryPort.buscarPorPrestadorId(usuario.getId());
        double promedio = calificaciones.stream()
                .filter(calificacion -> calificacion.getPuntaje() != null)
                .mapToInt(Calificacion::getPuntaje)
                .average()
                .orElse(0.0);

        return new PrestadorPublicoResult(
                usuario.getId(),
                usuario.getNombreUsuario(),
                nombreCompleto != null ? nombreCompleto.getNombre() : null,
                nombreCompleto != null ? nombreCompleto.getApellido() : null,
                perfil != null ? perfil.getFotoPerfilUrl() : null,
                perfil != null ? perfil.getDescripcionPersonal() : null,
                localidadPerfil(perfil),
                publicaciones.size(),
                categorias(publicaciones),
                servicios(publicaciones),
                zonasCobertura(publicaciones),
                precioDesde(publicaciones),
                publicacionesActivas(publicaciones),
                calificaciones.size(),
                Math.round(promedio * 10.0) / 10.0,
                resenasDestacadas(calificaciones)
        );
    }

    private boolean coincideNombreUsuario(Usuario usuario, String filtro) {
        if (filtro.isBlank()) {
            return true;
        }
        String nombreUsuario = usuario.getNombreUsuario();
        return nombreUsuario != null && nombreUsuario.toLowerCase(Locale.ROOT).contains(filtro);
    }

    private String ordenNombreUsuario(Usuario usuario) {
        return usuario.getNombreUsuario() != null ? usuario.getNombreUsuario() : "";
    }

    private String normalizarFiltro(String valor) {
        if (valor == null || valor.isBlank()) {
            return "";
        }
        return valor.trim().replace("@", "").toLowerCase(Locale.ROOT);
    }

    private String localidadPerfil(PerfilUsuario perfil) {
        if (perfil == null || perfil.getUbicacion() == null) {
            return null;
        }
        Ubicacion ubicacion = perfil.getUbicacion();
        if (ubicacion.getLocalidad() != null && !ubicacion.getLocalidad().isBlank()) {
            return ubicacion.getLocalidad();
        }
        return ubicacion.getCiudad();
    }

    private List<String> categorias(List<PublicacionServicio> publicaciones) {
        return publicaciones.stream()
                .map(PublicacionServicio::getCategoriaServicio)
                .filter(categoria -> categoria != null && categoria.getNombre() != null && !categoria.getNombre().isBlank())
                .map(categoria -> categoria.getNombre().trim())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    private List<String> servicios(List<PublicacionServicio> publicaciones) {
        return publicaciones.stream()
                .map(PublicacionServicio::getTitulo)
                .filter(titulo -> titulo != null && !titulo.isBlank())
                .map(String::trim)
                .distinct()
                .limit(4)
                .toList();
    }

    private List<String> zonasCobertura(List<PublicacionServicio> publicaciones) {
        return publicaciones.stream()
                .flatMap(publicacion -> {
                    List<Ubicacion> zonas = publicacion.getZonasCobertura();
                    if (zonas == null || zonas.isEmpty()) {
                        return publicacion.getUbicacion() == null ? List.<Ubicacion>of().stream() : List.of(publicacion.getUbicacion()).stream();
                    }
                    return zonas.stream();
                })
                .map(this::localidad)
                .filter(localidad -> localidad != null && !localidad.isBlank())
                .distinct()
                .limit(5)
                .toList();
    }

    private String localidad(Ubicacion ubicacion) {
        if (ubicacion.getLocalidad() != null && !ubicacion.getLocalidad().isBlank()) {
            return ubicacion.getLocalidad();
        }
        return ubicacion.getCiudad();
    }

    private BigDecimal precioDesde(List<PublicacionServicio> publicaciones) {
        return publicaciones.stream()
                .map(PublicacionServicio::getPrecioBase)
                .filter(precio -> precio != null && precio.compareTo(BigDecimal.ZERO) > 0)
                .min(BigDecimal::compareTo)
                .orElse(null);
    }

    private List<PrestadorPublicoResult.PublicacionActivaResult> publicacionesActivas(List<PublicacionServicio> publicaciones) {
        return publicaciones.stream()
                .map(publicacion -> new PrestadorPublicoResult.PublicacionActivaResult(
                        publicacion.getId(),
                        publicacion.getTitulo(),
                        publicacion.getDescripcion(),
                        publicacion.getCategoriaServicio() != null ? publicacion.getCategoriaServicio().getNombre() : null,
                        publicacion.getModalidadServicio(),
                        zonasPublicacion(publicacion),
                        publicacion.getPrecioBase()
                ))
                .toList();
    }

    private List<PrestadorPublicoResult.ResenaPublicaResult> resenasDestacadas(List<Calificacion> calificaciones) {
        return calificaciones.stream()
                .filter(calificacion -> calificacion.getComentario() != null && !calificacion.getComentario().isBlank())
                .sorted(Comparator.comparing(
                        Calificacion::getFechaCalificacion,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .map(calificacion -> new PrestadorPublicoResult.ResenaPublicaResult(
                        calificacion.getPuntaje(),
                        calificacion.getComentario(),
                        calificacion.getFechaCalificacion()
                ))
                .toList();
    }

    private List<String> zonasPublicacion(PublicacionServicio publicacion) {
        List<Ubicacion> zonas = publicacion.getZonasCobertura();
        if (zonas == null || zonas.isEmpty()) {
            zonas = publicacion.getUbicacion() == null ? List.of() : List.of(publicacion.getUbicacion());
        }
        return zonas.stream()
                .map(this::localidad)
                .filter(localidad -> localidad != null && !localidad.isBlank())
                .distinct()
                .toList();
    }
}
