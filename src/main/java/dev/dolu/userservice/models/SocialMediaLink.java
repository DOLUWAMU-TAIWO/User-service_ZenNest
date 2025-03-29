package dev.dolu.userservice.models;

import jakarta.persistence.*;

@Entity
@Table(name = "user_social_links")
public class SocialMediaLink {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String platform;
    private String url;

    public SocialMediaLink() {}

    public SocialMediaLink(String platform, String url) {
        this.platform = platform;
        this.url = url;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}