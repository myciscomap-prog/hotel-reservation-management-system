package com.hotelreserve.model;

import com.hotelreserve.exception.ValidationException;

import java.util.regex.Pattern;

public class Guest {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private final int id;
    private String fullName;
    private String email;
    private String phone;
    private String idNumber;

    public Guest(int id, String fullName, String email, String phone, String idNumber) throws ValidationException {
        validate(fullName, email, phone, idNumber);
        this.id = id;
        this.fullName = fullName.trim();
        this.email = email.trim();
        this.phone = phone.trim();
        this.idNumber = idNumber.trim();
    }

    private static void validate(String fullName, String email, String phone, String idNumber)
            throws ValidationException {
        if (fullName == null || fullName.isBlank()) {
            throw new ValidationException("Full name is required");
        }
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("A valid email address is required");
        }
        if (phone == null || phone.isBlank()) {
            throw new ValidationException("Phone number is required");
        }
        if (idNumber == null || idNumber.isBlank()) {
            throw new ValidationException("ID number is required");
        }
    }

    public int getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) throws ValidationException {
        if (fullName == null || fullName.isBlank()) {
            throw new ValidationException("Full name is required");
        }
        this.fullName = fullName.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) throws ValidationException {
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("A valid email address is required");
        }
        this.email = email.trim();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) throws ValidationException {
        if (phone == null || phone.isBlank()) {
            throw new ValidationException("Phone number is required");
        }
        this.phone = phone.trim();
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) throws ValidationException {
        if (idNumber == null || idNumber.isBlank()) {
            throw new ValidationException("ID number is required");
        }
        this.idNumber = idNumber.trim();
    }

    @Override
    public String toString() {
        return fullName + " (" + phone + ")";
    }
}
