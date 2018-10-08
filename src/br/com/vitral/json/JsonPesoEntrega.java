package br.com.vitral.json;

public class JsonPesoEntrega {

	private String nome;
	private Float peso;

	public JsonPesoEntrega(String nome, Float peso) {
		super();
		this.nome = nome;
		this.peso = peso;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Float getPeso() {
		return peso;
	}

	public void setPeso(Float peso) {
		this.peso = peso;
	}

	@Override
	public String toString() {
		return "JsonPesoEntrega [nome=" + nome + ", peso=" + peso + "]";
	}

}
