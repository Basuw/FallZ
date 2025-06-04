package com.fallz.backend;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fallz.backend.entities.Device;
import com.fallz.backend.entities.Person;
import com.fallz.backend.entities.User;
import com.fallz.backend.repositories.DeviceRepository;
import com.fallz.backend.repositories.PersonRepository;
import com.fallz.backend.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Test pour {@link DeviceController}.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class DeviceControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private DeviceRepository deviceRepository;
	@Autowired
	private PersonRepository personRepository;
	@Autowired
	private UserRepository userRepository;

	private final ObjectMapper objectMapper = new ObjectMapper();
	
	private User testUser;
	private Person testPerson;
	private Device testDevice;

	@BeforeEach
	void setUp() {
		deviceRepository.deleteAll();
		personRepository.deleteAll();
		userRepository.deleteAll();

		testUser = new User();
		testUser.setMail("john.doe@example.com");
		testUser.setPassword("password123");
		testUser = userRepository.save(testUser);

		testPerson = new Person();
		testPerson.setFirstname("John");
		testPerson.setLastname("Doe");
		testPerson.setUser(testUser);
		testPerson = personRepository.save(testPerson);

		testDevice = new Device();
		testDevice.setPerson(testPerson);
		testDevice.setId(UUID.randomUUID());
		testDevice = deviceRepository.save(testDevice);
	}

	/**
	 * Test pour la méthode {@link DeviceController#getDevices(String)}.
	 * 
	 * @throws Exception si une erreur se produit lors de l'exécution du test
	 */
	@Test
	public void getDevicesByPersonIdTest() throws Exception {
		mockMvc.perform(get("/device/" + testUser.getIdUser().toString()).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
				.andExpect(jsonPath("$[0].id").value(testDevice.getId().toString()))
				.andExpect(jsonPath("$[0].person.firstname").value(testPerson.getFirstname()))
				.andExpect(jsonPath("$[0].person.lastname").value(testPerson.getLastname()));
	}

	/**
	 * Test pour la méthode {@link DeviceController#getDevices(String)} avec un ID de personne qui n'existe pas.
	 * 
	 * @throws Exception si une erreur se produit lors de l'exécution du test
	 */
	@Test
	public void getDevicesByPersonIdNotFoundTest() throws Exception {
		UUID nonExistentUserId = UUID.randomUUID();

		mockMvc.perform(
				get("/device/" + nonExistentUserId.toString()).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isNotFound());
	}

	/** * Test pour la méthode {@link DeviceController#addDevice(String, Device)}.
	 * 
	 * @throws Exception si une erreur se produit lors de l'exécution du test
	 */
	@Test
    public void addDeviceTest() throws Exception {
        Device newDevice = new Device();
        newDevice.setId(UUID.randomUUID());
        newDevice.setPerson(Person.builder().firstname("TEst").lastname("api").build());

        mockMvc.perform(post("/device/" + testUser.getIdUser().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newDevice)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

	/**
	 * Test pour la méthode {@link DeviceController#addDevice(String, Device)} avec un utilisateur qui n'existe pas.
	 * 
	 * @throws Exception si une erreur se produit lors de l'exécution du test
	 */
    @Test
    public void addDeviceForNonExistentUserTest() throws Exception {
        UUID nonExistentUserId = UUID.randomUUID();
        Device newDevice = new Device();

        mockMvc.perform(post("/device/" + nonExistentUserId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newDevice)))
                .andExpect(status().isNotFound());
    }

    /**
	 * Test pour la méthode {@link DeviceController#addDevice(String, Device)} avec une personne qui n'existe pas.
	 * 
	 * @throws Exception si une erreur se produit lors de l'exécution du test
	 */
    @Test
    public void deleteDeviceTest() throws Exception {
        mockMvc.perform(delete("/device/" + testUser.getIdUser().toString() + "/" + testDevice.getId().toString()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/device/" + testUser.getIdUser().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    /**
	 * Test pour la méthode {@link DeviceController#deleteDevice(String, String)} avec un ID de device qui n'existe pas.
	 * 
	 * @throws Exception si une erreur se produit lors de l'exécution du test
	 */
    @Test
    public void deleteNonExistentDeviceTest() throws Exception {
        UUID nonExistentDeviceId = UUID.randomUUID();

        mockMvc.perform(delete("/device/" + testUser.getIdUser().toString() + "/" + nonExistentDeviceId.toString()))
                .andExpect(status().isNotFound());
    }

    /** * Test pour la méthode {@link DeviceController#deleteDevice(String, String)} avec un utilisateur qui n'existe pas.
	 * 
	 * @throws Exception si une erreur se produit lors de l'exécution du test
	 */
    @Test
    public void deleteDeviceForNonExistentUserTest() throws Exception {
        UUID nonExistentUserId = UUID.randomUUID();

        mockMvc.perform(delete("/device/" + nonExistentUserId.toString() + "/" + testDevice.getId().toString()))
                .andExpect(status().isNotFound());
    }
}