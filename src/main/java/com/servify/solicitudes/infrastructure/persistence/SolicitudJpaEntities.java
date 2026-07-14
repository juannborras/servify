package com.servify.solicitudes.infrastructure.persistence;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

// ── SolicitudServicioJpaEntity ──────────────────────────────
@Entity
@Table(name = "solicitud_servicio")
class SolicitudServicioJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "solicitante_id", nullable = false) private Long solicitanteId;
    @Column(name = "categoria_id", nullable = false) private Long categoriaId;
    @Column(name = "descripcion", nullable = false) private String descripcion;
    @Column(name = "modalidad", nullable = false) private String modalidad;
    @Column(name = "pais") private String pais;
    @Column(name = "provincia") private String provincia;
    @Column(name = "ciudad") private String ciudad;
    @Column(name = "localidad") private String localidad;
    @Column(name = "calle") private String calle;
    @Column(name = "altura") private String altura;
    @Column(name = "referencia") private String referencia;
    @Column(name = "latitud") private Double latitud;
    @Column(name = "longitud") private Double longitud;
    @Column(name = "disponibilidad_dia") private String disponibilidadDia;
    @Column(name = "disponibilidad_hora_desde") private LocalTime disponibilidadHoraDesde;
    @Column(name = "disponibilidad_hora_hasta") private LocalTime disponibilidadHoraHasta;
    @Column(name = "precio_referencia") private BigDecimal precioReferencia;
    @Column(name = "estado", nullable = false) private String estado;
    @Column(name = "fecha_solicitud") private LocalDateTime fechaSolicitud;
    @Column(name = "tipo_programacion", nullable = false) private String tipoProgramacion;
    @Column(name = "fecha_programada_inicio") private LocalDateTime fechaProgramadaInicio;
    @Column(name = "fecha_programada_fin") private LocalDateTime fechaProgramadaFin;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;

    protected SolicitudServicioJpaEntity() {}
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getSolicitanteId() { return solicitanteId; } public void setSolicitanteId(Long v) { this.solicitanteId = v; }
    public Long getCategoriaId() { return categoriaId; } public void setCategoriaId(Long v) { this.categoriaId = v; }
    public String getDescripcion() { return descripcion; } public void setDescripcion(String v) { this.descripcion = v; }
    public String getModalidad() { return modalidad; } public void setModalidad(String v) { this.modalidad = v; }
    public String getPais() { return pais; } public void setPais(String v) { this.pais = v; }
    public String getProvincia() { return provincia; } public void setProvincia(String v) { this.provincia = v; }
    public String getCiudad() { return ciudad; } public void setCiudad(String v) { this.ciudad = v; }
    public String getLocalidad() { return localidad; } public void setLocalidad(String v) { this.localidad = v; }
    public String getCalle() { return calle; } public void setCalle(String v) { this.calle = v; }
    public String getAltura() { return altura; } public void setAltura(String v) { this.altura = v; }
    public String getReferencia() { return referencia; } public void setReferencia(String v) { this.referencia = v; }
    public Double getLatitud() { return latitud; } public void setLatitud(Double v) { this.latitud = v; }
    public Double getLongitud() { return longitud; } public void setLongitud(Double v) { this.longitud = v; }
    public String getDisponibilidadDia() { return disponibilidadDia; } public void setDisponibilidadDia(String v) { this.disponibilidadDia = v; }
    public LocalTime getDisponibilidadHoraDesde() { return disponibilidadHoraDesde; } public void setDisponibilidadHoraDesde(LocalTime v) { this.disponibilidadHoraDesde = v; }
    public LocalTime getDisponibilidadHoraHasta() { return disponibilidadHoraHasta; } public void setDisponibilidadHoraHasta(LocalTime v) { this.disponibilidadHoraHasta = v; }
    public BigDecimal getPrecioReferencia() { return precioReferencia; } public void setPrecioReferencia(BigDecimal v) { this.precioReferencia = v; }
    public String getEstado() { return estado; } public void setEstado(String v) { this.estado = v; }
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; } public void setFechaSolicitud(LocalDateTime v) { this.fechaSolicitud = v; }
    public String getTipoProgramacion() { return tipoProgramacion; } public void setTipoProgramacion(String v) { this.tipoProgramacion = v; }
    public LocalDateTime getFechaProgramadaInicio() { return fechaProgramadaInicio; } public void setFechaProgramadaInicio(LocalDateTime v) { this.fechaProgramadaInicio = v; }
    public LocalDateTime getFechaProgramadaFin() { return fechaProgramadaFin; } public void setFechaProgramadaFin(LocalDateTime v) { this.fechaProgramadaFin = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }
}

// ── DistribucionSolicitudJpaEntity ──────────────────────────
@Entity
@Table(name = "distribucion_solicitud")
class DistribucionSolicitudJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "solicitud_id", nullable = false) private Long solicitudId;
    @Column(name = "publicacion_id", nullable = false) private Long publicacionId;
    @Column(name = "prestador_id", nullable = false) private Long prestadorId;
    @Column(name = "estado", nullable = false) private String estado;
    @Column(name = "ronda_distribucion") private Integer rondaDistribucion;
    @Column(name = "fecha_envio") private LocalDateTime fechaEnvio;
    @Column(name = "fecha_respuesta") private LocalDateTime fechaRespuesta;
    @Column(name = "fecha_expiracion") private LocalDateTime fechaExpiracion;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;

    protected DistribucionSolicitudJpaEntity() {}
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getSolicitudId() { return solicitudId; } public void setSolicitudId(Long v) { this.solicitudId = v; }
    public Long getPublicacionId() { return publicacionId; } public void setPublicacionId(Long v) { this.publicacionId = v; }
    public Long getPrestadorId() { return prestadorId; } public void setPrestadorId(Long v) { this.prestadorId = v; }
    public String getEstado() { return estado; } public void setEstado(String v) { this.estado = v; }
    public Integer getRondaDistribucion() { return rondaDistribucion; } public void setRondaDistribucion(Integer v) { this.rondaDistribucion = v; }
    public LocalDateTime getFechaEnvio() { return fechaEnvio; } public void setFechaEnvio(LocalDateTime v) { this.fechaEnvio = v; }
    public LocalDateTime getFechaRespuesta() { return fechaRespuesta; } public void setFechaRespuesta(LocalDateTime v) { this.fechaRespuesta = v; }
    public LocalDateTime getFechaExpiracion() { return fechaExpiracion; } public void setFechaExpiracion(LocalDateTime v) { this.fechaExpiracion = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}

// ── AsignacionServicioJpaEntity ──────────────────────────────
@Entity
@Table(name = "asignacion_servicio")
class AsignacionServicioJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "solicitud_id", nullable = false) private Long solicitudId;
    @Column(name = "distribucion_id", nullable = false) private Long distribucionId;
    @Column(name = "prestador_id", nullable = false) private Long prestadorId;
    @Column(name = "publicacion_id", nullable = false) private Long publicacionId;
    @Column(name = "precio_acordado") private BigDecimal precioAcordado;
    @Column(name = "estado", nullable = false) private String estado;
    @Column(name = "fecha_asignacion") private LocalDateTime fechaAsignacion;
    @Column(name = "fecha_finalizacion") private LocalDateTime fechaFinalizacion;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;

    protected AsignacionServicioJpaEntity() {}
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getSolicitudId() { return solicitudId; } public void setSolicitudId(Long v) { this.solicitudId = v; }
    public Long getDistribucionId() { return distribucionId; } public void setDistribucionId(Long v) { this.distribucionId = v; }
    public Long getPrestadorId() { return prestadorId; } public void setPrestadorId(Long v) { this.prestadorId = v; }
    public Long getPublicacionId() { return publicacionId; } public void setPublicacionId(Long v) { this.publicacionId = v; }
    public BigDecimal getPrecioAcordado() { return precioAcordado; } public void setPrecioAcordado(BigDecimal v) { this.precioAcordado = v; }
    public String getEstado() { return estado; } public void setEstado(String v) { this.estado = v; }
    public LocalDateTime getFechaAsignacion() { return fechaAsignacion; } public void setFechaAsignacion(LocalDateTime v) { this.fechaAsignacion = v; }
    public LocalDateTime getFechaFinalizacion() { return fechaFinalizacion; } public void setFechaFinalizacion(LocalDateTime v) { this.fechaFinalizacion = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }
}

// ── CalificacionJpaEntity ────────────────────────────────────
@Entity
@Table(
        name = "calificacion",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_calificacion_asignacion_rol",
                columnNames = {"asignacion_id", "rol_calificador"}
        )
)
class CalificacionJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "asignacion_id", nullable = false) private Long asignacionId;
    @Column(name = "calificador_id", nullable = false) private Long calificadorId;
    @Column(name = "calificado_id", nullable = false) private Long calificadoId;
    @Column(name = "rol_calificador", nullable = false) private String rolCalificador;
    @Column(name = "puntaje", nullable = false) private Integer puntaje;
    @Column(name = "comentario", length = 500) private String comentario;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;

    protected CalificacionJpaEntity() {}
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getAsignacionId() { return asignacionId; } public void setAsignacionId(Long v) { this.asignacionId = v; }
    public Long getCalificadorId() { return calificadorId; } public void setCalificadorId(Long v) { this.calificadorId = v; }
    public Long getCalificadoId() { return calificadoId; } public void setCalificadoId(Long v) { this.calificadoId = v; }
    public String getRolCalificador() { return rolCalificador; } public void setRolCalificador(String v) { this.rolCalificador = v; }
    public Integer getPuntaje() { return puntaje; } public void setPuntaje(Integer v) { this.puntaje = v; }
    public String getComentario() { return comentario; } public void setComentario(String v) { this.comentario = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
}

// ── ContraofertaJpaEntity ────────────────────────────────────
@Entity
@Table(name = "contraoferta")
class ContraofertaJpaEntity {
    @Id @Column(columnDefinition = "uuid") private UUID id;
    @Column(name = "distribucion_solicitud_id", nullable = false) private Long distribucionSolicitudId;
    @Column(name = "prestador_id", nullable = false) private Long prestadorId;
    @Column(name = "precio_original", nullable = false) private BigDecimal precioOriginal;
    @Column(name = "precio_propuesto", nullable = false) private BigDecimal precioPropuesto;
    @Column(name = "mensaje") private String mensaje;
    @Column(name = "estado", nullable = false) private String estado;
    @Column(name = "fecha_emision", nullable = false) private LocalDateTime fechaEmision;
    @Column(name = "fecha_resolucion") private LocalDateTime fechaResolucion;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;

    protected ContraofertaJpaEntity() {}
    public UUID getId() { return id; } public void setId(UUID id) { this.id = id; }
    public Long getDistribucionSolicitudId() { return distribucionSolicitudId; } public void setDistribucionSolicitudId(Long v) { this.distribucionSolicitudId = v; }
    public Long getPrestadorId() { return prestadorId; } public void setPrestadorId(Long v) { this.prestadorId = v; }
    public BigDecimal getPrecioOriginal() { return precioOriginal; } public void setPrecioOriginal(BigDecimal v) { this.precioOriginal = v; }
    public BigDecimal getPrecioPropuesto() { return precioPropuesto; } public void setPrecioPropuesto(BigDecimal v) { this.precioPropuesto = v; }
    public String getMensaje() { return mensaje; } public void setMensaje(String v) { this.mensaje = v; }
    public String getEstado() { return estado; } public void setEstado(String v) { this.estado = v; }
    public LocalDateTime getFechaEmision() { return fechaEmision; } public void setFechaEmision(LocalDateTime v) { this.fechaEmision = v; }
    public LocalDateTime getFechaResolucion() { return fechaResolucion; } public void setFechaResolucion(LocalDateTime v) { this.fechaResolucion = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }
}

// ── ConfirmacionFinalizacionJpaEntity ────────────────────────
@Entity
@Table(name = "confirmacion_finalizacion")
class ConfirmacionFinalizacionJpaEntity {
    @Id @Column(columnDefinition = "uuid") private UUID id;
    @Column(name = "solicitud_id", nullable = false) private Long solicitudId;
    @Column(name = "asignacion_servicio_id", nullable = false) private Long asignacionServicioId;
    @Column(name = "encuentro_servicio_id") private UUID encuentroServicioId;
    @Column(name = "confirmante_id", nullable = false) private Long confirmanteId;
    @Column(name = "rol_confirmante", nullable = false) private String rolConfirmante;
    @Column(name = "confirmada", nullable = false) private Boolean confirmada;
    @Column(name = "fecha_confirmacion") private LocalDateTime fechaConfirmacion;
    @Column(name = "observacion") private String observacion;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;

    protected ConfirmacionFinalizacionJpaEntity() {}
    public UUID getId() { return id; } public void setId(UUID id) { this.id = id; }
    public Long getSolicitudId() { return solicitudId; } public void setSolicitudId(Long v) { this.solicitudId = v; }
    public Long getAsignacionServicioId() { return asignacionServicioId; } public void setAsignacionServicioId(Long v) { this.asignacionServicioId = v; }
    public UUID getEncuentroServicioId() { return encuentroServicioId; } public void setEncuentroServicioId(UUID v) { this.encuentroServicioId = v; }
    public Long getConfirmanteId() { return confirmanteId; } public void setConfirmanteId(Long v) { this.confirmanteId = v; }
    public String getRolConfirmante() { return rolConfirmante; } public void setRolConfirmante(String v) { this.rolConfirmante = v; }
    public Boolean getConfirmada() { return confirmada; } public void setConfirmada(Boolean v) { this.confirmada = v; }
    public LocalDateTime getFechaConfirmacion() { return fechaConfirmacion; } public void setFechaConfirmacion(LocalDateTime v) { this.fechaConfirmacion = v; }
    public String getObservacion() { return observacion; } public void setObservacion(String v) { this.observacion = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }
}

@Entity
@Table(name = "servicio_encuentro")
class ServicioEncuentroJpaEntity {
    @Id @Column(columnDefinition = "uuid") private UUID id;
    @Column(name = "solicitud_id", nullable = false) private Long solicitudId;
    @Column(name = "asignacion_servicio_id") private Long asignacionServicioId;
    @Column(name = "recurrencia_servicio_id") private UUID recurrenciaServicioId;
    @Column(name = "propuesto_por_id", nullable = false) private Long propuestoPorId;
    @Column(name = "fecha_inicio", nullable = false) private LocalDateTime fechaInicio;
    @Column(name = "fecha_fin", nullable = false) private LocalDateTime fechaFin;
    @Column(name = "estado", nullable = false) private String estado;
    @Column(name = "mensaje") private String mensaje;
    @Column(name = "fecha_resolucion") private LocalDateTime fechaResolucion;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;

    protected ServicioEncuentroJpaEntity() {}
    public UUID getId() { return id; } public void setId(UUID id) { this.id = id; }
    public Long getSolicitudId() { return solicitudId; } public void setSolicitudId(Long v) { this.solicitudId = v; }
    public Long getAsignacionServicioId() { return asignacionServicioId; } public void setAsignacionServicioId(Long v) { this.asignacionServicioId = v; }
    public UUID getRecurrenciaServicioId() { return recurrenciaServicioId; } public void setRecurrenciaServicioId(UUID v) { this.recurrenciaServicioId = v; }
    public Long getPropuestoPorId() { return propuestoPorId; } public void setPropuestoPorId(Long v) { this.propuestoPorId = v; }
    public LocalDateTime getFechaInicio() { return fechaInicio; } public void setFechaInicio(LocalDateTime v) { this.fechaInicio = v; }
    public LocalDateTime getFechaFin() { return fechaFin; } public void setFechaFin(LocalDateTime v) { this.fechaFin = v; }
    public String getEstado() { return estado; } public void setEstado(String v) { this.estado = v; }
    public String getMensaje() { return mensaje; } public void setMensaje(String v) { this.mensaje = v; }
    public LocalDateTime getFechaResolucion() { return fechaResolucion; } public void setFechaResolucion(LocalDateTime v) { this.fechaResolucion = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }
}

@Entity
@Table(name = "servicio_recurrencia")
class ServicioRecurrenciaJpaEntity {
    @Id @Column(columnDefinition = "uuid") private UUID id;
    @Column(name = "solicitud_id", nullable = false) private Long solicitudId;
    @Column(name = "asignacion_servicio_id") private Long asignacionServicioId;
    @Column(name = "frecuencia", nullable = false) private String frecuencia;
    @Column(name = "dia_semana", nullable = false) private String diaSemana;
    @Column(name = "hora_desde", nullable = false) private LocalTime horaDesde;
    @Column(name = "hora_hasta", nullable = false) private LocalTime horaHasta;
    @Column(name = "fecha_inicio", nullable = false) private LocalDate fechaInicio;
    @Column(name = "fecha_fin") private LocalDate fechaFin;
    @Column(name = "estado", nullable = false) private String estado;
    @Column(name = "cancelada_por_id") private Long canceladaPorId;
    @Column(name = "fecha_cancelacion") private LocalDateTime fechaCancelacion;
    @Column(name = "motivo_cancelacion") private String motivoCancelacion;
    @Column(name = "created_at", nullable = false) private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private LocalDateTime updatedAt;

    protected ServicioRecurrenciaJpaEntity() {}
    public UUID getId() { return id; } public void setId(UUID id) { this.id = id; }
    public Long getSolicitudId() { return solicitudId; } public void setSolicitudId(Long v) { this.solicitudId = v; }
    public Long getAsignacionServicioId() { return asignacionServicioId; } public void setAsignacionServicioId(Long v) { this.asignacionServicioId = v; }
    public String getFrecuencia() { return frecuencia; } public void setFrecuencia(String v) { this.frecuencia = v; }
    public String getDiaSemana() { return diaSemana; } public void setDiaSemana(String v) { this.diaSemana = v; }
    public LocalTime getHoraDesde() { return horaDesde; } public void setHoraDesde(LocalTime v) { this.horaDesde = v; }
    public LocalTime getHoraHasta() { return horaHasta; } public void setHoraHasta(LocalTime v) { this.horaHasta = v; }
    public LocalDate getFechaInicio() { return fechaInicio; } public void setFechaInicio(LocalDate v) { this.fechaInicio = v; }
    public LocalDate getFechaFin() { return fechaFin; } public void setFechaFin(LocalDate v) { this.fechaFin = v; }
    public String getEstado() { return estado; } public void setEstado(String v) { this.estado = v; }
    public Long getCanceladaPorId() { return canceladaPorId; } public void setCanceladaPorId(Long v) { this.canceladaPorId = v; }
    public LocalDateTime getFechaCancelacion() { return fechaCancelacion; } public void setFechaCancelacion(LocalDateTime v) { this.fechaCancelacion = v; }
    public String getMotivoCancelacion() { return motivoCancelacion; } public void setMotivoCancelacion(String v) { this.motivoCancelacion = v; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
    public LocalDateTime getUpdatedAt() { return updatedAt; } public void setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }
}
