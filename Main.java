import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/* ===================== Interfaz ===================== */
interface Notificable {
    void recibirMensaje(String mensaje);
}

/* ===================== Enumeración ===================== */
enum EstadoServicio {
    SOLICITADO, PROGRAMADO, EN_ATENCION, FINALIZADO, CANCELADO
}

/* ===================== Usuario (abstracta) ===================== */
abstract class Usuario {
    private String identificacion;
    private String nombre;
    private String correoElectronico;

    public Usuario(String identificacion, String nombre, String correoElectronico) {
        this.identificacion = identificacion;
        this.nombre = nombre;
        this.correoElectronico = correoElectronico;
    }

    public String getIdentificacion() { return identificacion; }
    public void setIdentificacion(String identificacion) { this.identificacion = identificacion; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCorreoElectronico() { return correoElectronico; }
    public void setCorreoElectronico(String correoElectronico) { this.correoElectronico = correoElectronico; }
}

/* ===================== Paciente ===================== */
class Paciente extends Usuario implements Notificable {
    private String telefono;
    private String direccionPrincipal;
    private List<ServicioDomiciliario> servicios = new ArrayList<>();

    public Paciente(String identificacion, String nombre, String correoElectronico,
                    String telefono, String direccionPrincipal) {
        super(identificacion, nombre, correoElectronico);
        this.telefono = telefono;
        this.direccionPrincipal = direccionPrincipal;
    }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccionPrincipal() { return direccionPrincipal; }
    public void setDireccionPrincipal(String direccionPrincipal) { this.direccionPrincipal = direccionPrincipal; }
    public List<ServicioDomiciliario> getServicios() { return servicios; }

    /** El paciente solicita un servicio; queda en estado SOLICITADO. */
    public ServicioDomiciliario solicitarServicio(String codigo, LocalDateTime fechaHoraProgramada,
                                                  String direccionAtencion, String motivoSolicitud) {
        ServicioDomiciliario s = new ServicioDomiciliario(codigo, fechaHoraProgramada,
                direccionAtencion, motivoSolicitud, this);
        servicios.add(s);
        return s;
    }

    @Override
    public void recibirMensaje(String mensaje) {
        System.out.println("[Notificación a paciente " + getNombre() + " <" + getCorreoElectronico() + ">] " + mensaje);
    }
}

/* ===================== ProfesionalSalud ===================== */
class ProfesionalSalud extends Usuario implements Notificable {
    private String numeroRegistroProfesional;
    private String especialidad;
    private EquipoAtencion equipo; // 0..1, puede cambiar sin que el profesional deje de existir

    public ProfesionalSalud(String identificacion, String nombre, String correoElectronico,
                            String numeroRegistroProfesional, String especialidad) {
        super(identificacion, nombre, correoElectronico);
        this.numeroRegistroProfesional = numeroRegistroProfesional;
        this.especialidad = especialidad;
    }

    public String getNumeroRegistroProfesional() { return numeroRegistroProfesional; }
    public void setNumeroRegistroProfesional(String numeroRegistroProfesional) { this.numeroRegistroProfesional = numeroRegistroProfesional; }
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
    public EquipoAtencion getEquipo() { return equipo; }
    void setEquipo(EquipoAtencion equipo) { this.equipo = equipo; } // lo usa EquipoAtencion

    @Override
    public void recibirMensaje(String mensaje) {
        System.out.println("[Notificación a profesional " + getNombre() + " (" + especialidad + ")] " + mensaje);
    }
}

/* ===================== EquipoAtencion ===================== */
class EquipoAtencion {
    private String codigo;
    private String nombre;
    private String zonaCobertura;
    private List<ProfesionalSalud> profesionales = new ArrayList<>();

    public EquipoAtencion(String codigo, String nombre, String zonaCobertura) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.zonaCobertura = zonaCobertura;
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getZonaCobertura() { return zonaCobertura; }
    public void setZonaCobertura(String zonaCobertura) { this.zonaCobertura = zonaCobertura; }
    public List<ProfesionalSalud> getProfesionales() { return profesionales; }

    /** Si el profesional estaba en otro equipo, se mueve a este (sigue existiendo). */
    public void agregarProfesional(ProfesionalSalud profesional) {
        EquipoAtencion anterior = profesional.getEquipo();
        if (anterior != null && anterior != this) anterior.profesionales.remove(profesional);
        if (!profesionales.contains(profesional)) profesionales.add(profesional);
        profesional.setEquipo(this);
    }

    public void retirarProfesional(ProfesionalSalud profesional) {
        if (profesionales.remove(profesional)) profesional.setEquipo(null);
    }
}

/* ===================== ServicioDomiciliario ===================== */
class ServicioDomiciliario {
    private String codigo;
    private LocalDateTime fechaHoraProgramada;
    private String direccionAtencion;
    private String motivoSolicitud;
    private EstadoServicio estado;
    private Paciente paciente;              // 1
    private ProfesionalSalud profesional;   // 0..1
    private AtencionMedica atencion;        // 0..1 (composición)

    ServicioDomiciliario(String codigo, LocalDateTime fechaHoraProgramada, String direccionAtencion,
                         String motivoSolicitud, Paciente paciente) {
        this.codigo = codigo;
        this.fechaHoraProgramada = fechaHoraProgramada;
        this.direccionAtencion = direccionAtencion;
        this.motivoSolicitud = motivoSolicitud;
        this.paciente = paciente;
        this.estado = EstadoServicio.SOLICITADO;
    }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public LocalDateTime getFechaHoraProgramada() { return fechaHoraProgramada; }
    public void setFechaHoraProgramada(LocalDateTime f) { this.fechaHoraProgramada = f; }
    public String getDireccionAtencion() { return direccionAtencion; }
    public void setDireccionAtencion(String d) { this.direccionAtencion = d; }
    public String getMotivoSolicitud() { return motivoSolicitud; }
    public void setMotivoSolicitud(String m) { this.motivoSolicitud = m; }
    public EstadoServicio getEstado() { return estado; }
    public void setEstado(EstadoServicio estado) { this.estado = estado; }
    public Paciente getPaciente() { return paciente; }
    public ProfesionalSalud getProfesional() { return profesional; }
    public AtencionMedica getAtencion() { return atencion; }

    public void programar(ProfesionalSalud profesional) {
        if (estado != EstadoServicio.SOLICITADO)
            throw new IllegalStateException("Solo se puede programar un servicio SOLICITADO");
        this.profesional = profesional;
        this.estado = EstadoServicio.PROGRAMADO;
        paciente.recibirMensaje("Su servicio " + codigo + " fue programado con " + profesional.getNombre());
        profesional.recibirMensaje("Se le asignó el servicio " + codigo + " para " + paciente.getNombre());
    }

    /** La atención médica solo existe a través de un servicio (composición). */
    public AtencionMedica iniciarAtencion(LocalDateTime fechaHoraInicio) {
        if (estado != EstadoServicio.PROGRAMADO)
            throw new IllegalStateException("El servicio debe estar PROGRAMADO para iniciar la atención");
        this.atencion = new AtencionMedica(fechaHoraInicio);
        this.estado = EstadoServicio.EN_ATENCION;
        return atencion;
    }

    public void finalizar(LocalDateTime fechaHoraFin, String observaciones, String recomendaciones) {
        if (estado != EstadoServicio.EN_ATENCION)
            throw new IllegalStateException("El servicio debe estar EN_ATENCION para finalizar");
        atencion.setFechaHoraFin(fechaHoraFin);
        atencion.setObservacionesClinicas(observaciones);
        atencion.setRecomendaciones(recomendaciones);
        this.estado = EstadoServicio.FINALIZADO;
        paciente.recibirMensaje("Su servicio " + codigo + " ha finalizado.");
    }

    public void cancelar() {
        if (estado == EstadoServicio.FINALIZADO)
            throw new IllegalStateException("No se puede cancelar un servicio FINALIZADO");
        this.estado = EstadoServicio.CANCELADO;
        paciente.recibirMensaje("Su servicio " + codigo + " fue cancelado.");
    }
}

/* ===================== AtencionMedica ===================== */
class AtencionMedica {
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private String observacionesClinicas;
    private String recomendaciones;
    private List<MedicionSignosVitales> mediciones = new ArrayList<>(); // 0..*

    AtencionMedica(LocalDateTime fechaHoraInicio) { this.fechaHoraInicio = fechaHoraInicio; }

    public LocalDateTime getFechaHoraInicio() { return fechaHoraInicio; }
    public void setFechaHoraInicio(LocalDateTime f) { this.fechaHoraInicio = f; }
    public LocalDateTime getFechaHoraFin() { return fechaHoraFin; }
    public void setFechaHoraFin(LocalDateTime f) { this.fechaHoraFin = f; }
    public String getObservacionesClinicas() { return observacionesClinicas; }
    public void setObservacionesClinicas(String o) { this.observacionesClinicas = o; }
    public String getRecomendaciones() { return recomendaciones; }
    public void setRecomendaciones(String r) { this.recomendaciones = r; }
    public List<MedicionSignosVitales> getMediciones() { return mediciones; }

    public MedicionSignosVitales registrarMedicion(LocalDateTime fechaHora, double temperatura,
            int frecuenciaCardiaca, int presionSistolica, int presionDiastolica, double saturacionOxigeno) {
        MedicionSignosVitales m = new MedicionSignosVitales(fechaHora, temperatura, frecuenciaCardiaca,
                presionSistolica, presionDiastolica, saturacionOxigeno);
        mediciones.add(m);
        return m;
    }
}

/* ===================== MedicionSignosVitales ===================== */
class MedicionSignosVitales {
    private LocalDateTime fechaHora;
    private double temperatura;
    private int frecuenciaCardiaca;
    private int presionSistolica;
    private int presionDiastolica;
    private double saturacionOxigeno;

    MedicionSignosVitales(LocalDateTime fechaHora, double temperatura, int frecuenciaCardiaca,
                          int presionSistolica, int presionDiastolica, double saturacionOxigeno) {
        this.fechaHora = fechaHora;
        this.temperatura = temperatura;
        this.frecuenciaCardiaca = frecuenciaCardiaca;
        this.presionSistolica = presionSistolica;
        this.presionDiastolica = presionDiastolica;
        this.saturacionOxigeno = saturacionOxigeno;
    }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }
    public double getTemperatura() { return temperatura; }
    public void setTemperatura(double temperatura) { this.temperatura = temperatura; }
    public int getFrecuenciaCardiaca() { return frecuenciaCardiaca; }
    public void setFrecuenciaCardiaca(int f) { this.frecuenciaCardiaca = f; }
    public int getPresionSistolica() { return presionSistolica; }
    public void setPresionSistolica(int p) { this.presionSistolica = p; }
    public int getPresionDiastolica() { return presionDiastolica; }
    public void setPresionDiastolica(int p) { this.presionDiastolica = p; }
    public double getSaturacionOxigeno() { return saturacionOxigeno; }
    public void setSaturacionOxigeno(double s) { this.saturacionOxigeno = s; }

    @Override
    public String toString() {
        return String.format("%s | Temp: %.1f °C | FC: %d lpm | PA: %d/%d mmHg | SpO2: %.0f%%",
                fechaHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                temperatura, frecuenciaCardiaca, presionSistolica, presionDiastolica, saturacionOxigeno);
    }
}

/* ===================== Clase principal ===================== */
public class Main {
    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static void main(String[] args) {
        Paciente paciente = new Paciente("1085123456", "María Fernanda López", "maria.lopez@correo.com",
                "3101234567", "Calle 18 # 25-40, Pasto");
        ProfesionalSalud profesional = new ProfesionalSalud("98765432", "Dr. Carlos Ramírez",
                "carlos.ramirez@medihome.com", "RM-45821", "Medicina General");

        EquipoAtencion equipo = new EquipoAtencion("EQ-01", "Equipo Norte", "Norte de Pasto");
        equipo.agregarProfesional(profesional);

        ServicioDomiciliario servicio = paciente.solicitarServicio("SD-001",
                LocalDateTime.of(2026, 10, 10, 9, 0), "Calle 18 # 25-40, Pasto",
                "Control por cuadro gripal con fiebre");
        servicio.programar(profesional);

        AtencionMedica atencion = servicio.iniciarAtencion(LocalDateTime.of(2026, 10, 10, 9, 5));
        atencion.registrarMedicion(LocalDateTime.of(2026, 10, 10, 9, 10), 38.2, 92, 120, 80, 96);
        atencion.registrarMedicion(LocalDateTime.of(2026, 10, 10, 9, 40), 37.6, 85, 118, 78, 97);
        servicio.finalizar(LocalDateTime.of(2026, 10, 10, 9, 55),
                "Paciente con fiebre y congestión nasal, sin signos de dificultad respiratoria.",
                "Reposo, hidratación abundante y control en 48 horas si persiste la fiebre.");

        System.out.println();
        generarReporte(paciente);
    }

    /** Reporte de la atención prestada al paciente. */
    public static void generarReporte(Paciente p) {
        System.out.println("==================================================");
        System.out.println("        MEDIHOME - REPORTE DE ATENCIÓN MÉDICA");
        System.out.println("==================================================");
        System.out.println("PACIENTE");
        System.out.println("  Identificación : " + p.getIdentificacion());
        System.out.println("  Nombre         : " + p.getNombre());
        System.out.println("  Correo         : " + p.getCorreoElectronico());
        System.out.println("  Teléfono       : " + p.getTelefono());
        System.out.println("  Dirección      : " + p.getDireccionPrincipal());

        for (ServicioDomiciliario s : p.getServicios()) {
            System.out.println("--------------------------------------------------");
            System.out.println("SERVICIO " + s.getCodigo() + "  [" + s.getEstado() + "]");
            System.out.println("  Fecha programada : " + s.getFechaHoraProgramada().format(F));
            System.out.println("  Dirección        : " + s.getDireccionAtencion());
            System.out.println("  Motivo           : " + s.getMotivoSolicitud());

            ProfesionalSalud pr = s.getProfesional();
            if (pr != null) {
                System.out.println("PROFESIONAL");
                System.out.println("  Nombre           : " + pr.getNombre());
                System.out.println("  Especialidad     : " + pr.getEspecialidad());
                System.out.println("  Reg. profesional : " + pr.getNumeroRegistroProfesional());
                if (pr.getEquipo() != null)
                    System.out.println("  Equipo           : " + pr.getEquipo().getNombre()
                            + " (" + pr.getEquipo().getZonaCobertura() + ")");
            }

            AtencionMedica a = s.getAtencion();
            if (a != null) {
                System.out.println("ATENCIÓN MÉDICA");
                System.out.println("  Inicio          : " + a.getFechaHoraInicio().format(F));
                System.out.println("  Fin             : " + (a.getFechaHoraFin() == null ? "En curso" : a.getFechaHoraFin().format(F)));
                System.out.println("  Observaciones   : " + a.getObservacionesClinicas());
                System.out.println("  Recomendaciones : " + a.getRecomendaciones());
                System.out.println("  Signos vitales (" + a.getMediciones().size() + " mediciones):");
                int i = 1;
                for (MedicionSignosVitales m : a.getMediciones())
                    System.out.println("    " + (i++) + ". " + m);
            } else {
                System.out.println("  (Sin atención médica registrada)");
            }
        }
        System.out.println("==================================================");
    }
}
