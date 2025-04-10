package org.ictec.entities;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String email;

    private String username;
    private String password;
    private String nickName;
    private String profilePicture;
    private boolean isAdmin;

    public String getNickName() {
        return nickName;
    }

    // Getters, setters, constructors
}
