package com.co.personalsoft.kitdeemergencia;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.cloudant.client.api.Search;
import com.cloudant.client.api.model.SearchResult;
import com.co.personalsoft.kitdeemergencia.model.Actividades;
import com.co.personalsoft.kitdeemergencia.model.GeoReferenciaPlanes;
import com.co.personalsoft.kitdeemergencia.model.PlanActividades;
import com.co.personalsoft.kitdeemergencia.model.Puntos;
import com.google.gson.Gson;


@SpringBootApplication
@RestController
public class KitdeemergenciaApplication {

	@Autowired
	private CloudantClient client;
	final Gson gson = new Gson();
	private SearchResult<GeoReferenciaPlanes> srgeoreferenciaplanes;
	private GeoReferenciaPlanes georeferenciaplanes;
	private SearchResult<PlanActividades> srplanActividades;
	List<PlanActividades> lsplanActividades = new ArrayList<>();
	PlanActividades planActividades;
	private SearchResult<Actividades> srActividades;
	List<Actividades> lsActividades = new ArrayList<>();
	private Puntos formatPuntos;

	private String query;
	private Database db ;
	private Search search;

	public static void main(String[] args) { 
		SpringApplication.run(KitdeemergenciaApplication.class, args);
	}
	
	@GetMapping("/riesgos")
	String riesgos(@RequestParam(value="riesgo") String riesgo,
			@RequestParam(value="localizacion") String localizacion,
			@RequestParam(value="georeferencia", defaultValue = "\"\"") String georeferencia) {
		georeferenciaplanes=null;
		db = client.database("georeferenciaplanes", false);
		
		search = db.search("geoplan/geoplan");
		search.includeDocs(true);
		
		/*if (georeferencia.isEmpty()) {
			georeferencia="\"\"";
		}*/

		query = "localizacion:" +localizacion + " AND riesgo:" + riesgo + " AND georeferencia:"+  georeferencia;
		srgeoreferenciaplanes = search.querySearchResult(query, GeoReferenciaPlanes.class);
		
		for (SearchResult<GeoReferenciaPlanes>.SearchResultRow sr : srgeoreferenciaplanes.getRows()) {
			georeferenciaplanes = (GeoReferenciaPlanes) sr.getFields();
			break;
			}
		return "<table>" + georeferenciaplanes.toRow() + "</table>";
		//return gson.toJson(georeferenciaplanes); 	
		}
	@GetMapping("/planes")
	String riesgos(@RequestParam(value="riesgo") String riesgo,
			@RequestParam(value="plan") String plan) {
		lsplanActividades.clear();
		db = client.database("planactividad", false);
		
		search = db.search("planactividad/planactividad");
		search.includeDocs(true);
		query = "riesgo:"+ riesgo + " AND plan:" +plan;
		srplanActividades = search.querySearchResult(query, PlanActividades.class);
		String stringRows="";
		for (SearchResult<PlanActividades>.SearchResultRow sr : srplanActividades.getRows()) {
			planActividades = (PlanActividades) sr.getFields();
			stringRows+="<tr>";
			stringRows+="<td>Descripcion</td>";
			stringRows+="<td>"+planActividades.getDescipcion()+"</td>";
			stringRows+="<td>Tipo Actividad</td>";
			stringRows+="<td>"+planActividades.getTipo()+"</td>";
			stringRows+="<td><a href=\"localhost:8080/actividades?actividad="+planActividades.getActividad()+"\">Juega</a></td>";
			stringRows+="</tr>";
			lsplanActividades.add((PlanActividades) sr.getFields());
			
			}
		return "<table>"+stringRows+"</table>"; 	
		}
	
	@GetMapping("/actividades")
	String riesgos(@RequestParam(value="actividad") String actividad) {
		lsActividades.clear();
		db = client.database("actividades", false);
		
		search = db.search("actividades/actividades");
		search.includeDocs(true);
		query = "actividad:"+ actividad;	
		srActividades = search.querySearchResult(query, Actividades.class);
		
		for (SearchResult<Actividades>.SearchResultRow sr : srActividades.getRows()) {
			lsActividades.add(sr.getFields());
			}
		
		return gson.toJson(lsActividades); 	
		}
	
	@PostMapping("/puntos")
	Boolean puntos(@RequestParam("usuario") String usuario,
			@RequestParam("georeferencia") String georeferencia,
			@RequestParam("localizacion") String localizacion,
			@RequestParam("fecha") String fecha,
			@RequestParam("riesgo") String riesgo,
			@RequestParam("actividad") String actividad,
			@RequestParam("plan") String plan,
			@RequestParam("puntos") String puntos
			) {
		db = client.database("puntos", false);
		formatPuntos = new Puntos();
		formatPuntos.setUsuario(usuario);
		formatPuntos.setPlan(plan);
		formatPuntos.setGeoreferencia(georeferencia);
		formatPuntos.setLocalizacion(localizacion);
		formatPuntos.setFecha(fecha);
		formatPuntos.setRiesgo(riesgo);
		formatPuntos.setActividad(actividad);
		formatPuntos.setPuntos(puntos);
		db.save(formatPuntos);
		return true; 	
		}

}
