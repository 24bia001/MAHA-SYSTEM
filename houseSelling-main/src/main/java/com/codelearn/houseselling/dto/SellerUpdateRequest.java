package com.codelearn.houseselling.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SellerUpdateRequest {
    @NotBlank private String name;
    @NotBlank @Email private String email;
    @NotBlank private String phone;
    @NotBlank private String address;
    @NotBlank private String nida;
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    private String image;
    public String getName(){return name;} public void setName(String v){name=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
    public String getAddress(){return address;} public void setAddress(String v){address=v;}
    public String getNida(){return nida;} public void setNida(String v){nida=v;}
    public String getPassword(){return password;} public void setPassword(String v){password=v;}
    public String getImage(){return image;} public void setImage(String v){image=v;}
}
