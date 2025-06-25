// package com.sunway.course.timetable.integration.repository;
// import java.util.List;
// import java.util.Optional;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import org.junit.jupiter.api.DisplayName;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.autoconfigure.domain.EntityScan;
// import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
// import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// import com.sunway.course.timetable.model.Module;
// import com.sunway.course.timetable.repository.ModuleRepository;

// @DataJpaTest(
//     properties = {
//         // auto-create/drop schema so H2 has MODULE table
//         "spring.jpa.hibernate.ddl-auto=create-drop"
//     }
// )
// @EntityScan(basePackageClasses = Module.class)
// @EnableJpaRepositories(basePackageClasses = ModuleRepository.class)
// class ModuleRepositoryTest {

//     @Autowired
//     private ModuleRepository repo;

//     @Test
//     @DisplayName("Save and retrieve a Module by ID")
//     void testSaveAndFindById() {
//         Module m = new Module();
//         m.setId("MATH101");
//         m.setName("Calculus I");
//         m.setCreditHour(3);

//         // save
//         repo.save(m);

//         // findById
//         Optional<Module> found = repo.findById("MATH101");
//         assertTrue(found.isPresent(), "Module should be found by ID");
//         assertEquals("Calculus I", found.get().getName());
//         assertEquals(3, found.get().getCreditHour());
//     }

//     @Test
//     @DisplayName("findByName returns correct Module")
//     void testFindByName() {
//         Module m1 = new Module();
//         m1.setId("PHY101");
//         m1.setName("Physics I");
//         m1.setCreditHour(4);
//         repo.save(m1);

//         Optional<Module> opt = repo.findByName("Physics I");
//         assertTrue(opt.isPresent(), "findByName should return the saved module");
//         assertEquals("PHY101", opt.get().getId());
//     }

//     @Test
//     @DisplayName("findByCreditHour returns all modules with that credit hour")
//     void testFindByCreditHour() {
//         Module a = new Module(); a.setId("CHEM101"); a.setName("Chemistry I"); a.setCreditHour(3);
//         Module b = new Module(); b.setId("BIO101");  b.setName("Biology I");   b.setCreditHour(3);
//         Module c = new Module(); c.setId("ENG101");  c.setName("English I");   c.setCreditHour(2);

//         repo.saveAll(List.of(a, b, c));

//         Optional<List<Module>> threeCredit  = repo.findByCreditHour(3);
//         Optional<List<Module>> twoCredit    = repo.findByCreditHour(2);
//         Optional<List<Module>> fiveCredit   = repo.findByCreditHour(5);

//         assertTrue(threeCredit.isPresent(), "Should find modules with 3 credit hours");
//         List<Module> list3 = threeCredit.get();
//         assertEquals(2, list3.size());
//         assertTrue(list3.stream().anyMatch(m -> m.getId().equals("CHEM101")));
//         assertTrue(list3.stream().anyMatch(m -> m.getId().equals("BIO101")));

//         assertTrue(twoCredit.isPresent(), "Should find modules with 2 credit hours");
//         assertEquals(1, twoCredit.get().size());
//         assertEquals("ENG101", twoCredit.get().get(0).getId());

//         assertTrue(fiveCredit.isPresent(), "Query for non-existent credit hour should return empty list");
//         assertTrue(fiveCredit.get().isEmpty());
//     }

//     @Test
//     @DisplayName("Deleting a Module removes it from the repository")
//     void testDelete() {
//         Module m = new Module();
//         m.setId("HIST101");
//         m.setName("History I");
//         m.setCreditHour(3);
//         repo.save(m);

//         assertTrue(repo.findById("HIST101").isPresent());
//         repo.deleteById("HIST101");
//         assertFalse(repo.findById("HIST101").isPresent());
//     }
// }

