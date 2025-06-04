package com.fallz.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.fallz.backend.entities.Paiement;
import com.fallz.backend.entities.User;
import com.fallz.backend.repositories.PaiementRepository;
import com.fallz.backend.repositories.UserRepository;

/**
 * Test pour {@link com.fallz.backend.controllers.PaiementController}.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class PaiementControllerTest {

	@Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaiementRepository paiementRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        paiementRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setMail("user.with.bill@example.com");
        testUser.setPassword(passwordEncoder.encode("securePass"));
        testUser.setPassword("securePass");
        testUser = userRepository.save(testUser);
        testUserId = testUser.getIdUser();
    }

    /**
	 * Test de la méthode {@link com.fallz.backend.controllers.PaiementController#getBill(String)}.
	 */
    @Test
    public void getBillSuccessfullyTest() throws Exception {
        Paiement paiement1 = new Paiement();
        paiement1.setUser(testUser);
        paiement1.setAmount(new BigDecimal("50.00"));
        paiement1.setPaid(false);
        paiement1.setDate(LocalDateTime.now().minusDays(10));
        paiementRepository.save(paiement1);

        Paiement paiementPaid = new Paiement();
        paiementPaid.setUser(testUser);
        paiementPaid.setAmount(new BigDecimal("10.00"));
        paiementPaid.setPaid(true);
        paiementPaid.setDate(LocalDateTime.now().minusDays(2));
        paiementRepository.save(paiementPaid);

        BigDecimal expectedTotalBill = new BigDecimal("50.00");

        mockMvc.perform(get("/paiement/" + testUserId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedTotalBill.toString()));
    }

    /**
	 * Test de la méthode {@link com.fallz.backend.controllers.PaiementController#getBill(String)}
	 * pour un utilisateur sans paiements.
	 */
    @Test
    public void getBillForUserWithNoPaiementsTest() throws Exception {
        mockMvc.perform(get("/paiement/" + testUserId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /**
     * Test de la méthode {@link com.fallz.backend.controllers.PaiementController#getBill(String)}.
     */
    @Test
    public void getBillForNonExistentUserTest() throws Exception {
        UUID nonExistentUserId = UUID.randomUUID();

        mockMvc.perform(get("/paiement/" + nonExistentUserId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}