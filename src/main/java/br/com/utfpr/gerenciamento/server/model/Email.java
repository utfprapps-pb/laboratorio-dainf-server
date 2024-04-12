package br.com.utfpr.gerenciamento.server.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
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

}
