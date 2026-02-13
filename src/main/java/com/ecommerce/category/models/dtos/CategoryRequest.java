package com.ecommerce.category.models.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CategoryRequest {
    @NotBlank(message = "El nombre es obligatorio.")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres.")
    private String name;
    @NotBlank(message = "La descripción es obligatoria.")
    @Size(min = 20, max =255, message = "La descripción no puede tener menos de 20 y exceder 255 caracteres.")
    private String description;
    private String image;
    private boolean status;

    public CategoryRequest() {
    }

    public CategoryRequest(String name, String description, String image, boolean status) {
        this.name = name;
        this.description = description;
        this.image = image;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isStatus() {
        return status;
    }
}
