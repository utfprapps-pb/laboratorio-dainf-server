package br.com.utfpr.gerenciamento.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Email {

    private String de;
    private String para;
    private String titulo;
    private String conteudo;
    private Map<String, byte[]> fileMap = new HashMap<>();
    private List<String> paraList = new ArrayList<>();

    public Email addFile(String fileName, byte[] fileBytes) {
        fileMap.put(fileName, fileBytes);
        return this;
    }

    public String getDe() {
        return de;
    }

    public Email setDe(String de) {
        this.de = de;
        return this;
    }

    public String getPara() {
        return para;
    }

    public Email setPara(String para) {
        this.para = para;
        return this;
    }

    public String getTitulo() {
        return titulo;
    }

    public Email setTitulo(String titulo) {
        this.titulo = titulo;
        return this;
    }

    public String getConteudo() {
        return conteudo;
    }

    public Email setConteudo(String conteudo) {
        this.conteudo = conteudo;
        return this;
    }

    public List<String> getParaList() {
        return paraList;
    }

    public Email setParaList(List<String> paraList) {
        this.paraList = paraList;
        return this;
    }
}
