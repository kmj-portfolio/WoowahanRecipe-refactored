package _4.NovemberRecipeMarket.controller.api;

import _4.NovemberRecipeMarket.domain.dto.Response;
import _4.NovemberRecipeMarket.domain.dto.item.*;
import _4.NovemberRecipeMarket.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/items")
@RequiredArgsConstructor
public class ItemRestController {

    private final ItemService itemService;

    // 재료 단건 상세 조회
    @GetMapping("/{itemId}")
    public Response<ItemGetResponse> getItemById(@PathVariable Long itemId) {
        ItemGetResponse itemGetResponse = itemService.getItem(itemId);
        return Response.success(itemGetResponse);
    }


    // 재료 등록 (관리자)
    @PostMapping
    public Response<ItemResponse> createItem(Authentication authentication,
                                             @Valid @RequestBody ItemRequest itemRequest) {
        ItemResponse itemResponse = itemService.createItem(authentication.getName(), itemRequest);
        return Response.success(itemResponse);
    }

    // 재료
    @PutMapping("/{itemId}")
    public Response<ItemResponse> updateItem(@PathVariable Long sellerId, @PathVariable Long itemId,
                                             Authentication authentication, @Valid @RequestBody ItemRequest itemRequest) {
        ItemResponse itemResponse = itemService.updateItem(authentication.getName(), itemId, itemRequest);
        return Response.success(itemResponse);
    }

    @DeleteMapping("/{itemId}")
    public Response<ItemDeleteResponse> deleteItem(@PathVariable Long itemId, Authentication authentication) {
        ItemDeleteResponse itemDeleteResponse = itemService.deleteItem(authentication.getName(), itemId);
        return Response.success(itemDeleteResponse);
    }

    // 특정 판매자의 모든 상품 조회
    @GetMapping("/sellers/{sellerId}")
    public Response<Page<ItemGetResponse>> getAllItemsBySeller(@PathVariable Long sellerId, Pageable pageable) {
        Page<ItemGetResponse> allItemsBySeller = itemService.getAllItemsBySeller(sellerId, pageable);
        return Response.success(allItemsBySeller);
    }

    // 재료 전체 조회
    @GetMapping("/items/list")
    public Response<Page<ItemGetResponse>> getAllItems(Pageable pageable) {
        Page<ItemGetResponse> itemGetResponses = itemService.getAllItems(pageable);
        return Response.success(itemGetResponses);
    }

    // 재료 검색
    @GetMapping("/items/search")
    public Response<Page<ItemListResponse>> searchItem(@RequestBody String keyword, Pageable pageable){
        return Response.success(itemService.searchByKeyword(keyword, pageable));
    }

    // TODO: 관리자 재료 수정, 삭제, 등록
}