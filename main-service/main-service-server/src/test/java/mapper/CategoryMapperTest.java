package mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.dto.CategoryDto;
import ru.practicum.dto.NewCategoryDto;
import ru.practicum.mapper.CategoryMapper;
import ru.practicum.model.Category;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тесты для CategoryMapper")
public class CategoryMapperTest {

    private final CategoryMapper categoryMapper = new CategoryMapper();

    @Test
    @DisplayName("Преобразование Category в CategoryDto - успешное преобразование")
    void toDto_shouldMapCategoryToCategoryDto() {
        Category category = Category.builder()
                .id(1L)
                .name("Test Category")
                .build();

        CategoryDto result = categoryMapper.toDto(category);

        assertNotNull(result);
        assertEquals(category.getId(), result.getId());
        assertEquals(category.getName(), result.getName());
    }

    @Test
    @DisplayName("Преобразование NewCategoryDto в Category - успешное преобразование")
    void toEntity_shouldMapNewCategoryDtoToCategory() {
        NewCategoryDto dto = new NewCategoryDto();
        dto.setName("Test Category");

        Category result = categoryMapper.toEntity(dto);

        assertNotNull(result);
        assertEquals(dto.getName(), result.getName());
    }
}
