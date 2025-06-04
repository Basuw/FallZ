package com.fallz.backend;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.fallz.backend.controllers.LoginController;
import com.fallz.backend.dto.ConnectDTO;
import com.fallz.backend.dto.RegisterDTO;
import com.fallz.backend.entities.User;
import com.fallz.backend.repositories.PersonRepository;
import com.fallz.backend.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test pour {@link LoginController}.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PersonRepository personRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        personRepository.deleteAll();
    }

    /**
	 * Test de la méthode {@link LoginController#registerUser(RegisterDTO)}.
	 */
    @Test
    public void registerUserSuccessTest() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setMail("newuser@example.com");
        registerDTO.setPassword("securePassword123");
        registerDTO.setFirstname("Jane");
        registerDTO.setLastname("Doe");

        mockMvc.perform(post("/login/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUser").exists())
                .andExpect(jsonPath("$.mail").value("newuser@example.com"));

        Optional<User> createdUser = userRepository.findByMail("newuser@example.com");
        assertThat(createdUser).isPresent();
        assertThat(createdUser.get().getMail()).isEqualTo("newuser@example.com");
    }

    /**
     * Test de la méthode {@link LoginController#registerUser(RegisterDTO)} avec un email déjà existant.
     */
    @Test
    public void registerUserWithExistingMailTest() throws Exception {
    	
        User existingUser = new User();
        existingUser.setMail("existing@example.com");
        existingUser.setPassword(passwordEncoder.encode("hashedpassword"));
        userRepository.save(existingUser);

        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setMail("existing@example.com");
        registerDTO.setPassword("anotherPassword");

        mockMvc.perform(post("/login/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isBadRequest());
    }

    /**
	 * Test de la méthode {@link LoginController#registerUser(RegisterDTO)} avec des données invalides.
	 */
    @Test
    public void registerUserWithInvalidDataTest() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setMail("invalid-mail");
        registerDTO.setPassword("short");

        mockMvc.perform(post("/login/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isBadRequest());
    }

    /**
	 * Test de la méthode {@link LoginController#connectUser(ConnectDTO)}.
	 */
    @Test
    public void connectUserSuccessTest() throws Exception {
        User userToConnect = new User();
        userToConnect.setMail("test@example.com");
        userToConnect.setPassword(passwordEncoder.encode("plainPassword123"));
        userRepository.save(userToConnect);

        ConnectDTO connectDTO = new ConnectDTO();
        connectDTO.setMail("test@example.com");
        connectDTO.setPassword("plainPassword123");

        mockMvc.perform(post("/login/connect")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(connectDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idUser").value(userToConnect.getIdUser().toString()))
                .andExpect(jsonPath("$.mail").value("test@example.com"));
    }

    /**
	 * Test de la méthode {@link LoginController#connectUser(ConnectDTO)} avec des données invalides.
	 */
    @Test
    public void connectUserNotFoundTest() throws Exception {
        ConnectDTO connectDTO = new ConnectDTO();
        connectDTO.setMail("nonexistent@example.com");
        connectDTO.setPassword("somePassword");

        mockMvc.perform(post("/login/connect")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(connectDTO)))
                .andExpect(status().isForbidden());
    }

    /**
	 * Test de la méthode {@link LoginController#connectUser(ConnectDTO)} avec des identifiants invalides.
	 */
    @Test
    public void connectUserInvalidCredentialsTest() throws Exception {
        User userToConnect = new User();
        userToConnect.setMail("valid@example.com");
        userToConnect.setPassword("correctPassword");
        userRepository.save(userToConnect);

        ConnectDTO connectDTO = new ConnectDTO();
        connectDTO.setMail("valid@example.com");
        userToConnect.setPassword(passwordEncoder.encode("correctPassword"));

        mockMvc.perform(post("/login/connect")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(connectDTO)))
                .andExpect(status().isBadRequest());
        }

    /** 
     * Test de la méthode {@link LoginController#connectUser(ConnectDTO)} avec des données invalides.
	 */
    @Test
    public void connectUserWithInvalidDataTest() throws Exception {
        ConnectDTO connectDTO = new ConnectDTO();
        connectDTO.setMail("invalid-mail");
        connectDTO.setPassword("");

        mockMvc.perform(post("/login/connect")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(connectDTO)))
                .andExpect(status().isBadRequest());
    }
}