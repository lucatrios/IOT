package dispositivo.componentes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import dispositivo.api.iot.infraestructure.Dispositivo_RegistradorMQTT;
import dispositivo.api.mqtt.Dispositivo_APIMQTT;
import dispositivo.api.mqtt.PublishInfo_APIMQTT;
import dispositivo.api.rest.Dispositivo_APIREST;
import dispositivo.interfaces.IDispositivo;
import dispositivo.interfaces.IFuncion;

public class Dispositivo implements IDispositivo {
	
	protected String deviceId = null;
	protected Boolean habilitado = true;
	protected Map<String, IFuncion> functions = null;
	protected Dispositivo_RegistradorMQTT registrador = null;
	protected Dispositivo_APIMQTT apiFuncionesMQTT = null;
	protected Dispositivo_APIREST apiFuncionesREST = null;
	protected PublishInfo_APIMQTT publishInfoApiMQTT = null;

	public static Dispositivo build(String deviceId, String ip, int port, String mqttBrokerURL) {
		Dispositivo dispositivo = new Dispositivo(deviceId);
		dispositivo.registrador = Dispositivo_RegistradorMQTT.build(deviceId, ip, mqttBrokerURL);
		dispositivo.apiFuncionesMQTT = Dispositivo_APIMQTT.build(dispositivo, mqttBrokerURL);
		dispositivo.apiFuncionesREST = Dispositivo_APIREST.build(dispositivo, port);
		dispositivo.publishInfoApiMQTT = PublishInfo_APIMQTT.build(deviceId, ip, mqttBrokerURL);
		return dispositivo;
	}

	protected Dispositivo(String deviceId) {
		this.deviceId = deviceId;
	}
	
	@Override
	public String getId() {
		return this.deviceId;
	}

	protected Map<String, IFuncion> getFunctions() {
		return this.functions;
	}
	
	protected void setFunctions(Map<String, IFuncion> fs) {
		this.functions = fs;
	}
	
	@Override
	public Collection<IFuncion> getFunciones() {
		if ( this.getFunctions() == null )
			return null;
		return this.getFunctions().values();
	}
	
	
	@Override
	public IDispositivo addFuncion(IFuncion f) {
		if ( this.getFunctions() == null )
			this.setFunctions(new HashMap<String, IFuncion>());
		this.getFunctions().put(f.getId(), f);
		return this;
	}
	
	
	@Override
	public IFuncion getFuncion(String funcionId) {
		if ( this.getFunctions() == null )
			return null;
		return this.getFunctions().get(funcionId);
	}
	
		
	@Override
	public IDispositivo iniciar() {
		for(IFuncion f : this.getFunciones()) {
			f.iniciar();
		}

		this.registrador.registrar();
		this.apiFuncionesMQTT.iniciar();
		this.apiFuncionesREST.iniciar();
		return this;
	}

	@Override
	public IDispositivo detener() {
		this.registrador.desregistrar();
		this.apiFuncionesMQTT.detener();
		this.apiFuncionesREST.detener();
		for(IFuncion f : this.getFunciones()) {
			f.detener();
		}
		return this;
	}

	@Override
	public IDispositivo habilita() {
		this.habilitado = true;
		return this;
	}

	@Override
	public IDispositivo deshabilita() {
		this.habilitado = false;
		return this;
	}

	@Override
	public Boolean getHabilitado() {
		return habilitado;
	}

	@Override
	public void publishFunctionInfo(IFuncion f) {
		this.publishInfoApiMQTT.publishFunctionInfo(f);
	}
}
