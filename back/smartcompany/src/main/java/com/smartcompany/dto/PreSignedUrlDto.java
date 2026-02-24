package com.smartcompany.dto;

import java.net.URL;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PreSignedUrlDto {

    private URL url;
    private String filename;

}
