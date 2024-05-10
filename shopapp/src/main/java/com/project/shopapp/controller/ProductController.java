package com.project.shopapp.controller;

import jakarta.validation.Path;
import org.springframework.util.StringUtils;
import com.project.shopapp.dtos.ProductDTO;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@RestController
@RequestMapping("api/v1/products")
public class ProductController {

    @GetMapping() // http://localhost:8088/api/v1/products?page=1&limit=10
    public ResponseEntity<String> getProducts(
            @RequestParam("page") int page,
            @RequestParam("limit") int limit
    ){
        return ResponseEntity.ok("getProducts here");
    }
    @PostMapping(value = "",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(
            @Valid @ModelAttribute ProductDTO productDTO,
            BindingResult result
    ){
        try{
            if(result.hasErrors()){
                List<String> errorMessages =result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return  ResponseEntity.badRequest().body(errorMessages);
            }
            List<MultipartFile> files=productDTO.getFiles();
            files=files==null? new ArrayList<MultipartFile>():files;
            for(MultipartFile file : files){
                if(file.getSize()==0){
                    continue;
                }
               if(file.getSize()>10*1024*1024){
                   return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                           .body("File is to large: Maximum size is 10MB");
               }
               String contentType=file.getContentType();// lấy định dạng file
                if(contentType==null||!contentType.startsWith("image/")){
                    return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                            .body("File mus be an image");
                }
                String filename=storeFile(file);

            }
            return ResponseEntity.ok("product created successfully");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    private String storeFile(MultipartFile file) throws IOException{
        String filename= StringUtils.cleanPath(file.getOriginalFilename());
        String uniqueFilename= UUID.randomUUID().toString()+"_"+filename;
        java.nio.file.Path uploadDir= Paths.get("uploads");
        if(!Files.exists(uploadDir)){
            Files.createDirectories(uploadDir);
        }
        // duong dan day du ten file
        java.nio.file.Path destination = Paths.get(uploadDir.toString(),uniqueFilename);
        // sao chep file vao thu muc dich
        Files.copy(file.getInputStream(),destination, StandardCopyOption.REPLACE_EXISTING);
        return uniqueFilename;
    }
    @GetMapping("/{id}")
    public ResponseEntity<String> getProductById(@PathVariable("id") String productId){
        return ResponseEntity.ok("Product with ID : "+productId);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable long id){
        return ResponseEntity.ok(String.format("Product with id =%d deleted successfully",id));
    }

}
