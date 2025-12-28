package _4.NovemberRecipeMarket.service;

import _4.NovemberRecipeMarket.domain.dto.item.ItemRequest;
import _4.NovemberRecipeMarket.domain.dto.item.ItemResponse;
import _4.NovemberRecipeMarket.domain.entity.Item;
import _4.NovemberRecipeMarket.domain.entity.Seller;
import _4.NovemberRecipeMarket.repository.ItemRepository;
import _4.NovemberRecipeMarket.repository.SellerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private SellerRepository sellerRepository;

    @InjectMocks
    private ItemService itemService;

    @Mock
    private Seller mockSeller;

    @Mock
    private Item mockItem;

    private final String USERNAME = "seller1";
    private final Long ITEM_ID = 1L;
    private final Long SELLER_ID = 1L;
    private final String ITEM_NAME = "Test Item";
    private final int PRICE = 1000;
    private final int STOCK = 10;

    private ItemRequest itemRequest;

    @BeforeEach
    void before() {
        itemRequest = new ItemRequest(ITEM_NAME, PRICE, STOCK);
    }

    @Test
    @DisplayName("상품 등록 성공")
    void create_item_success() {
        when(sellerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(mockSeller));
        when(itemRepository.save(any())).thenReturn(mockItem);

        ItemResponse response = itemService.createItem(USERNAME, itemRequest);

        assertEquals("상품이 등록되었습니다.", response.getMessage());
        assertEquals(ITEM_NAME, response.getItemName());
        assertEquals(STOCK, response.getStock());
        assertEquals(PRICE, response.getPrice());
        verify(itemRepository).save(any(Item.class));
    }

    @Test
    @DisplayName("상품 수정 성공")
    void update_item_success() {
        when(sellerRepository.findByUsername(USERNAME)).thenReturn(Optional.of(mockSeller));
        when(itemRepository.findById(ITEM_ID)).thenReturn(Optional.of(mockItem));
        when(mockItem.getSeller()).thenReturn(mockSeller);

        ItemRequest request = new ItemRequest("변경된 이름", 1000, 100);

        ItemResponse response = itemService.updateItem(USERNAME, ITEM_ID, request);
        assertEquals("변경된 이름", request.getItemName());
    }
}