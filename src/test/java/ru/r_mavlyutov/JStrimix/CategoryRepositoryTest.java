package ru.r_mavlyutov.JStrimix;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import ru.r_mavlyutov.JStrimix.dao.CategoryRepository;
import ru.r_mavlyutov.JStrimix.entity.Category;

@DataJpaTest
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void testFindByName() {
        // arrange
        Category cat = new Category();
        cat.setName("Science");
        categoryRepository.save(cat);

        // act
        var found = categoryRepository.findByName("Science");

        // assert
        Assertions.assertTrue(found.isPresent());
        Assertions.assertEquals(cat.getId(), found.get().getId());
    }

    @Test
    void testUniqueNameConstraint() {
        Category c1 = new Category();
        c1.setName("Music");
        categoryRepository.save(c1);

        Category c2 = new Category();
        c2.setName("Music");
        Assertions.assertThrows(Exception.class, () -> categoryRepository.saveAndFlush(c2));
    }
}
