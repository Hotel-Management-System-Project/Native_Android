package com.stayflow.customer.model;
public class SignupRequest{ public String fullName,email,password,phone,role="CUSTOMER",emailVerificationToken; public SignupRequest(){} public SignupRequest(String fullName,String email,String password,String phone){this.fullName=fullName;this.email=email;this.password=password;this.phone=phone;} }
