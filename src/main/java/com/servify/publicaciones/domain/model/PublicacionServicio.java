package com.servify.publicaciones.domain.model;


import com.servify.publicaciones.domain.enumtype.EstadoPublicacion;
import com.servify.shared.domain.enumtype.ModalidadServicio;
import com.servify.shared.domain.valueobject.DisponibilidadHoraria;
import com.servify.shared.domain.model.BaseEntity;
import com.servify.shared.domain.valueobject.Ubicacion;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PublicacionServicio extends BaseEntity {

    private UUID usuarioId;
    private CategoriaServicio categoriaServicio;
    private String titulo;
    private String descripcion;
    private ModalidadServicio modalidadServicio;
    private Ubicacion ubicacion;
    private List<Ubicacion> zonasCobertura;
    private List<DisponibilidadHoraria> disponibilidadesHorarias;
    private BigDecimal precioBase;
    private EstadoPublicacion estado;

    protected PublicacionServicio() {
    }

    public PublicacionServicio(UUID id,
                               UUID usuarioId,
                               CategoriaServicio categoriaServicio,
                               String titulo,
                               String descripcion,
                               ModalidadServicio modalidadServicio,
                               Ubicacion ubicacion,
                               List<DisponibilidadHoraria> disponibilidadesHorarias,
                               BigDecimal precioBase,
                               EstadoPublicacion estado) {
        this(
                id,
                usuarioId,
                categoriaServicio,
                titulo,
                descripcion,
                modalidadServicio,
                ubicacion,
                ubicacion == null ? List.of() : List.of(ubicacion),
                disponibilidadesHorarias,
                precioBase,
                estado
        );
    }

    public PublicacionServicio(UUID id,
                               UUID usuarioId,
                               CategoriaServicio categoriaServicio,
                               String titulo,
                               String descripcion,
                               ModalidadServicio modalidadServicio,
                               Ubicacion ubicacion,
                               List<Ubicacion> zonasCobertura,
                               List<DisponibilidadHoraria> disponibilidadesHorarias,
                               BigDecimal precioBase,
                               EstadoPublicacion estado) {
        super(id);
        this.usuarioId = usuarioId;
        this.categoriaServicio = categoriaServicio;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.modalidadServicio = modalidadServicio;
        this.ubicacion = ubicacion;
        this.zonasCobertura = normalizarZonasCobertura(ubicacion, zonasCobertura);
        this.disponibilidadesHorarias = disponibilidadesHorarias;
        this.precioBase = precioBase;
        this.estado = estado;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public CategoriaServicio getCategoriaServicio() {
        return categoriaServicio;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public ModalidadServicio getModalidadServicio() {
        return modalidadServicio;
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public List<Ubicacion> getZonasCobertura() {
        return zonasCobertura == null ? List.of() : zonasCobertura;
    }

    public List<Ubicacion> getUbicacionesParaMatching() {
        List<Ubicacion> zonas = getZonasCobertura();
        if (!zonas.isEmpty()) {
            return zonas;
        }
        return ubicacion == null ? List.of() : List.of(ubicacion);
    }

    public List<DisponibilidadHoraria> getDisponibilidadesHorarias() {
        return disponibilidadesHorarias;
    }

    public BigDecimal getPrecioBase() {
        return precioBase;
    }

    public EstadoPublicacion getEstado() {
        return estado;
    }

    // Devuelve true si la publicación está habilitada
    public boolean estaActiva() {
        return EstadoPublicacion.ACTIVA.equals(this.estado);
    }

    // Verifica si la publicación pertenece al usuario indicado
    public boolean perteneceA(UUID usuarioId) {
        return this.usuarioId != null && this.usuarioId.equals(usuarioId);
    }

    // Valida que la publicación pueda participar en distribución de solicitudes
    public boolean puedeParticiparEnDistribucion() {
        if (!estaActiva()) {
            return false;
        }
        return cumpleCondicionesParaActivarse();
    }

    public boolean cumpleCondicionesParaActivarse() {
        if (categoriaServicio == null || !categoriaServicio.estaActiva()) {
            return false;
        }
        if (disponibilidadesHorarias == null || disponibilidadesHorarias.isEmpty()) {
            return false;
        }
        boolean tieneDisponibilidadValida = disponibilidadesHorarias.stream()
                .anyMatch(DisponibilidadHoraria::esRangoHorarioValido);
        if (!tieneDisponibilidadValida) {
            return false;
        }
        if (modalidadServicio == null) {
            return false;
        }
        if ((ModalidadServicio.PRESENCIAL.equals(modalidadServicio) ||
                ModalidadServicio.MIXTA.equals(modalidadServicio))
                && getUbicacionesParaMatching().stream().noneMatch(Ubicacion::esAptaParaBusquedaGeografica)) {
            return false;
        }
        return true;
    }

    // Actualiza el título validando que no sea nulo ni vacío
    public void actualizarTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new IllegalArgumentException("El título no puede ser nulo ni vacío.");
        }
        if (titulo.length() > 100) {
            throw new IllegalArgumentException("El título no puede superar los 100 caracteres.");
        }
        this.titulo = titulo.trim();
    }

    // Actualiza la descripción validando longitud y contenido
    public void actualizarDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("La descripción no puede ser nula ni vacía.");
        }
        if (descripcion.length() > 1000) {
            throw new IllegalArgumentException("La descripción no puede superar los 1000 caracteres.");
        }
        this.descripcion = descripcion.trim();
    }

    // Actualiza la categoría validando que exista y esté activa
    public void actualizarCategoria(CategoriaServicio categoriaServicio) {
        if (categoriaServicio == null) {
            throw new IllegalArgumentException("La categoría no puede ser nula.");
        }
        if (!categoriaServicio.estaActiva()) {
            throw new IllegalStateException("La categoría no está disponible para su uso.");
        }
        this.categoriaServicio = categoriaServicio;
    }

    // Actualiza la modalidad validando que no sea nula
    public void actualizarModalidad(ModalidadServicio modalidadServicio) {
        if (modalidadServicio == null) {
            throw new IllegalArgumentException("La modalidad no puede ser nula.");
        }
        this.modalidadServicio = modalidadServicio;
    }

    // Actualiza la ubicación validando coherencia con la modalidad
    public void actualizarUbicacion(Ubicacion ubicacion) {
        if ((ModalidadServicio.PRESENCIAL.equals(this.modalidadServicio) ||
                ModalidadServicio.MIXTA.equals(this.modalidadServicio))) {
            if (ubicacion == null || !ubicacion.esAptaParaBusquedaGeografica()) {
                throw new IllegalArgumentException("La ubicación es requerida y debe ser válida para modalidad presencial o mixta.");
            }
        }
        this.ubicacion = ubicacion;
        this.zonasCobertura = normalizarZonasCobertura(ubicacion, this.zonasCobertura);
    }

    public void actualizarZonasCobertura(List<Ubicacion> zonasCobertura) {
        List<Ubicacion> normalizadas = normalizarZonasCobertura(this.ubicacion, zonasCobertura);
        if ((ModalidadServicio.PRESENCIAL.equals(this.modalidadServicio) ||
                ModalidadServicio.MIXTA.equals(this.modalidadServicio))
                && normalizadas.stream().noneMatch(Ubicacion::esAptaParaBusquedaGeografica)) {
            throw new IllegalArgumentException("Debe informar al menos una zona valida para modalidad presencial o mixta.");
        }
        this.zonasCobertura = normalizadas;
        if ((this.ubicacion == null || !this.ubicacion.esAptaParaBusquedaGeografica()) && !normalizadas.isEmpty()) {
            this.ubicacion = normalizadas.get(0);
        }
    }

    // Actualiza disponibilidades validando que no haya superposiciones
    public void actualizarDisponibilidades(List<DisponibilidadHoraria> disponibilidadesHorarias) {
        if (disponibilidadesHorarias == null || disponibilidadesHorarias.isEmpty()) {
            throw new IllegalArgumentException("La lista de disponibilidades no puede ser nula ni vacía.");
        }
        for (DisponibilidadHoraria d : disponibilidadesHorarias) {
            if (!d.esRangoHorarioValido()) {
                throw new IllegalArgumentException("Una o más disponibilidades tienen un rango horario inválido.");
            }
        }
        for (int i = 0; i < disponibilidadesHorarias.size(); i++) {
            for (int j = i + 1; j < disponibilidadesHorarias.size(); j++) {
                if (disponibilidadesHorarias.get(i).seSuperponeCon(disponibilidadesHorarias.get(j))) {
                    throw new IllegalArgumentException("Existen disponibilidades horarias superpuestas.");
                }
            }
        }
        this.disponibilidadesHorarias = disponibilidadesHorarias;
    }

    // Actualiza el precio validando que sea positivo
    public void actualizarPrecioBase(BigDecimal precioBase) {
        if (precioBase == null) {
            throw new IllegalArgumentException("El precio base no puede ser nulo.");
        }
        if (precioBase.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio base no puede ser negativo.");
        }
        this.precioBase = precioBase;
    }

    // Cambia el estado a ACTIVA verificando condiciones mínimas
    public void activar() {
        if (EstadoPublicacion.ACTIVA.equals(this.estado)) {
            throw new IllegalStateException("La publicación ya se encuentra activa.");
        }
        if (EstadoPublicacion.ELIMINADA.equals(this.estado)) {
            throw new IllegalStateException("No se puede activar una publicación eliminada.");
        }
        if (!cumpleCondicionesParaActivarse()) {
            throw new IllegalStateException("La publicación no cumple las condiciones mínimas para activarse.");
        }
        this.estado = EstadoPublicacion.ACTIVA;
    }

    // Cambia el estado a INACTIVA
    public void desactivar() {
        if (EstadoPublicacion.INACTIVA.equals(this.estado)) {
            throw new IllegalStateException("La publicación ya se encuentra inactiva.");
        }
        if (EstadoPublicacion.ELIMINADA.equals(this.estado)) {
            throw new IllegalStateException("No se puede desactivar una publicación eliminada.");
        }
        this.estado = EstadoPublicacion.INACTIVA;
    }

    // Cambia el estado a PAUSADA
    public void pausar() {
        if (EstadoPublicacion.PAUSADA.equals(this.estado)) {
            throw new IllegalStateException("La publicación ya se encuentra pausada.");
        }
        if (EstadoPublicacion.ELIMINADA.equals(this.estado)) {
            throw new IllegalStateException("No se puede pausar una publicación eliminada.");
        }
        this.estado = EstadoPublicacion.PAUSADA;
    }

    // Marca la publicación como ELIMINADA (baja lógica)
    public void eliminar() {
        if (EstadoPublicacion.ELIMINADA.equals(this.estado)) {
            throw new IllegalStateException("La publicación ya se encuentra eliminada.");
        }
        this.estado = EstadoPublicacion.ELIMINADA;
    }

    private List<Ubicacion> normalizarZonasCobertura(Ubicacion ubicacionPrincipal, List<Ubicacion> zonas) {
        Map<String, Ubicacion> unicas = new LinkedHashMap<>();
        if (zonas != null) {
            for (Ubicacion zona : zonas) {
                agregarZona(unicas, zona);
            }
        }
        if (unicas.isEmpty()) {
            agregarZona(unicas, ubicacionPrincipal);
        }
        return List.copyOf(unicas.values());
    }

    private void agregarZona(Map<String, Ubicacion> destino, Ubicacion zona) {
        if (zona == null) {
            return;
        }
        destino.putIfAbsent(claveZona(zona), zona);
    }

    private String claveZona(Ubicacion zona) {
        return normalizar(zona.getPais()) + "|" +
                normalizar(zona.getProvincia()) + "|" +
                normalizar(zona.getCiudad()) + "|" +
                normalizar(zona.getLocalidad()) + "|" +
                zona.getLatitud() + "|" +
                zona.getLongitud();
    }

    private String normalizar(String valor) {
        return valor == null ? "" : valor.trim().toLowerCase();
    }
}
