package fr.insat.om2m.tp2.test;

import obix.io.ObixEncoder;
import org.eclipse.om2m.commons.resource.AE;
import org.eclipse.om2m.commons.resource.Container;
import org.eclipse.om2m.commons.resource.ContentInstance;

import fr.insat.om2m.tp2.mapper.Mapper;
import fr.insat.om2m.tp2.mapper.MapperInterface;
import fr.insat.om2m.tp2.client.Response;
import fr.insat.om2m.tp2.util.RequestLoader;
import fr.insat.om2m.tp2.client.Client;

import java.io.IOException;

import obix.Obj;
import obix.Str;
import obix.Int;
import obix.io.ObixDecoder;

public class MapperTest {

	public static void main(String[] args) throws IOException {
		MapperInterface mapper = new Mapper();

		// example to test marshal operation
		AE ae = new AE();
		ae.setRequestReachability(false);
		String xml = mapper.marshal(ae);
		System.out.println(xml);

		// get the XML representation, parse it with unmarshal operation
		AE ae2 = (AE) mapper.unmarshal(xml);
		System.out.println(ae2);

		Client cl = new Client();
		Response res;

		// 1) Create an APPLICATION ENTITY (AE) without specifying any name for it nor AE id. A unique
		// resource id (ri) and a unique Application Entity ID (aei) will be generated by the oneM2M platform.
		AE ae3 = new AE();
		ae3.setAppID("app-test2");
		ae3.setRequestReachability(false);
		res = cl.create("http://localhost:8080/~/in-cse", mapper.marshal(ae3), "admin:admin", "2");

		// 2) Create a CONTAINER (CNT) without specifying a name, under the previously created AE. To do
		//	this, you have to get the resource ID given by the platform once the AE is created and use it as the
		// location for your container creation.
		AE ae4 = (AE) mapper.unmarshal(res.getRepresentation());

		Container cnt = new Container();
		res = cl.create("http://localhost:8080/~/in-cse/" + ae4.getAEID(), mapper.marshal(cnt), "admin:admin", "3");
		Container cnt2 = (Container) mapper.unmarshal(res.getRepresentation());

		// 3) Create a CONTENT INSTANCE (CIN), without specifying any name, under the previously created
		// CNT. To do this you will need the resource id of the CNT as before. the content Instance should
		// contain the following oBIX content: (use the oBIX API to create an obj and serialize it)
		ContentInstance cin = new ContentInstance();

		Int value = new Int(666);
		Str location = new Str("INSA");
		Obj payload = new Obj();
		payload.add("value", value);
		payload.add("location", location);

		cin.setContent(ObixEncoder.toString(payload));
		res = cl.create("http://localhost:8080/~" + cnt2.getResourceID(), mapper.marshal(cin), "admin:admin", "4");
		ContentInstance cin2 = (ContentInstance) mapper.unmarshal(res.getRepresentation());

		// 4) Retrieve the CIN resource, decode the oBIX content and print the sensor data value.
		res = cl.retrieve("http://localhost:8080/~" + cin2.getResourceID(), "admin:admin");
		ContentInstance cin3 = (ContentInstance) mapper.unmarshal(res.getRepresentation());

		Obj chargeUtile = ObixDecoder.fromString(cin3.getContent());
		System.out.println("Valeur capteur: " + chargeUtile.get("value"));
		System.out.println("Localisation capteur: " + chargeUtile.get("location"));
	}
	
}
