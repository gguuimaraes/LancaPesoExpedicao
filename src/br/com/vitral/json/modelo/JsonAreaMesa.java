package br.com.vitral.json.modelo;

import java.util.Date;

public class JsonAreaMesa {
	private Integer codmesa;
	private Float area;
	private Date data;
	
	public JsonAreaMesa(Integer codmesa, Float area, Date data) {
		super();
		this.codmesa = codmesa;
		this.area = area;
		this.data = data;
	}

	public Integer getCodmesa() {
		return codmesa;
	}

	public void setCodmesa(Integer codmesa) {
		this.codmesa = codmesa;
	}

	public Float getArea() {
		return area;
	}

	public void setArea(Float area) {
		this.area = area;
	}

	public Date getData() {
		return data;
	}

	public void setData(Date data) {
		this.data = data;
	}

}
