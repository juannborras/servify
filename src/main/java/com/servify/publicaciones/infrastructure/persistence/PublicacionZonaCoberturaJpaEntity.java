package com.servify.publicaciones.infrastructure.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "publicacion_zona_cobertura")
public class PublicacionZonaCoberturaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "publicacion_id", nullable = false)
    private Long publicacionId;

    @Column(name = "pais")
    private String pais;

    @Column(name = "provincia")
    private String provincia;

    @Column(name = "ciudad")
    private String ciudad;

    @Column(name = "localidad")
    private String localidad;

    @Column(name = "calle")
    private String calle;

    @Column(name = "altura")
    private String altura;

    @Column(name = "referencia")
    private String referencia;

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;

    protected PublicacionZonaCoberturaJpaEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPublicacionId() { return publicacionId; }
    public void setPublicacionId(Long publicacionId) { this.publicacionId = publicacionId; }
    public String getPais() { return pais; }
    public void setPais(String pais) { this.pais = pais; }
    public String getProvincia() { return provincia; }
    public void setProvincia(String provincia) { this.provincia = provincia; }
    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }
    public String getLocalidad() { return localidad; }
    public void setLocalidad(String localidad) { this.localidad = localidad; }
    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }
    public String getAltura() { return altura; }
    public void setAltura(String altura) { this.altura = altura; }
    public String getReferencia() { return referencia; }
    public void setReferencia(String referencia) { this.referencia = referencia; }
    public Double getLatitud() { return latitud; }
    public void setLatitud(Double latitud) { this.latitud = latitud; }
    public Double getLongitud() { return longitud; }
    public void setLongitud(Double longitud) { this.longitud = longitud; }
}
