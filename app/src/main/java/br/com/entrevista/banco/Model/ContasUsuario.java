package br.com.entrevista.banco.Model;

import java.io.Serializable;

public class ContasUsuario implements Serializable {

    private Integer Idusuario;
    private String nome;
    private String CodBanco;
    private String agencia;
    private String  balanco;

    public Integer getIdusuario() {
        return Idusuario;
    }

    public void setIdusuario(Integer idusuario) {
        Idusuario = idusuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCodBanco() {
        return CodBanco;
    }

    public void setCodBanco(String codBanco) {
        CodBanco = codBanco;
    }

    public String getAgencia() {
        return agencia;
    }
    

    public void setAgencia(String agencia) {
        this.agencia = agencia;
    }

    public String  getBalanco() {
        return balanco;
    }

    public void setBalanco(String  balanco) {
        this.balanco = balanco;
    }


}
