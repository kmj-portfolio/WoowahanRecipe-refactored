package _4.NovemberRecipeMarket.service;

import _4.NovemberRecipeMarket.domain.dto.item.*;
import _4.NovemberRecipeMarket.domain.enums.UserRole;
import _4.NovemberRecipeMarket.domain.entity.Item;
import _4.NovemberRecipeMarket.domain.entity.Seller;
import _4.NovemberRecipeMarket.exception.AppException;
import _4.NovemberRecipeMarket.exception.ErrorCode;
import _4.NovemberRecipeMarket.repository.ItemRepository;
import _4.NovemberRecipeMarket.repository.SellerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ItemService {

    private final ItemRepository itemRepository;
    private final SellerRepository sellerRepository;

    @Transactional(readOnly = true)
    public ItemGetResponse getItem(Long itemId) {
        Item item = validateItemById(itemId);
        return toItemResponseForList(item);
    }

    public ItemResponse createItem(String username, ItemRequest itemRequest) {
        Seller seller = validateSellerByUsername(username);
        Item item = new Item(seller, itemRequest.getItemName(), itemRequest.getPrice(), itemRequest.getStock());
        itemRepository.save(item);
        log.info("Item saved with ID: {}", item.getId()); // ← ID가 할당되는지 확인
        return toItemResponse(item, "상품 등록이 완료되었습니다");
    }

    public ItemResponse updateItem(String username, Long itemId, ItemRequest itemRequest) {
        Seller seller = validateSellerByUsername(username);
        Item item = validateItemById(itemId);
        hasPermission(seller, item);
        item.updateItem(itemRequest.getItemName(), itemRequest.getPrice(), itemRequest.getStock());
        return toItemResponse(item, "상품 수정이 완료되었습니다.");
    }

    public ItemDeleteResponse deleteItem(String username, Long itemId) {
        Seller seller = validateSellerByUsername(username);
        Item item = validateItemById(itemId);
        hasPermission(seller, item);
        itemRepository.delete(item);
        return new ItemDeleteResponse(itemId);
    }

    @Transactional(readOnly = true)
    public Page<ItemGetResponse> getAllItemsBySeller(Long sellerId,Pageable pageable) {
        Seller seller = validateSellerById(sellerId);
        return itemRepository.findAllBySeller(seller, pageable).map(this::toItemResponseForList);
    }

    @Transactional(readOnly = true)
    public Page<ItemGetResponse> getAllItems(Pageable pageable) {
        return itemRepository.findAll(pageable).map(this::toItemResponseForList);
    }

    // 재료 페이지에서 재료 검색
    public Page<ItemListResponse> searchByKeyword(String keyword, Pageable pageable) {
        Page<Item> items = itemRepository.findByItemNameContaining(keyword, pageable);
        if (items.getSize() == 0) {
            throw new AppException(ErrorCode.ITEM_NOT_FOUND, ErrorCode.ITEM_NOT_FOUND.getMessage());
        }
        return items.map(this::toItemListResponse);
    }

    private ItemListResponse toItemListResponse(Item item) {
        return ItemListResponse.builder()
                .id(item.getId())
                .itemImagePath(item.getImagePath()) //TODO: image path
                .itemName(item.getItemName())
                .price(item.getPrice())
                .build();
    }



    private ItemGetResponse toItemResponseForList(Item item) {
        return new ItemGetResponse(item.getId(), item.getItemName(), item.getPrice(),
                item.getStock());
    }

    private ItemResponse toItemResponse(Item item, String message) {
        return new ItemResponse(item.getId(), item.getItemName(), item.getPrice(), item.getStock(),
                message);
    }

    private void hasPermission(Seller seller, Item item) {
        if (!seller.getId().equals(item.getSeller().getId()) && !isAdmin(seller)) {
            throw new AppException(ErrorCode.INVALID_PERMISSION);
        }
    }

    private boolean isAdmin(Seller seller) {
        return seller.getUserRole() == UserRole.ADMIN;
    }

    private Item validateItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new AppException(ErrorCode.ITEM_NOT_FOUND));
    }

    private Seller validateSeller(Long sellerId, String username) {
        Seller seller = validateSellerById(sellerId);
        if (seller.getUsername().equals(username)) {
            return seller;
        }

        Seller loggedInSeller = validateSellerByUsername(username);
        if (isAdmin(loggedInSeller)) {
            return seller;
        }
        throw new AppException(ErrorCode.INVALID_PERMISSION);
    }

    private Seller validateSellerByUsername(String username) {
        return sellerRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_NOT_FOUND));
    }

    private Seller validateSellerById(Long sellerId) {
        return sellerRepository.findById(sellerId)
                .orElseThrow(() -> new AppException(ErrorCode.SELLER_NOT_FOUND));
    }
}
