package _4.NovemberRecipeMarket.repository.order;

import _4.NovemberRecipeMarket.domain.entity.*;
import _4.NovemberRecipeMarket.domain.enums.OrderStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static _4.NovemberRecipeMarket.domain.entity.QItem.item;
import static _4.NovemberRecipeMarket.domain.entity.QOrder.order;
import static _4.NovemberRecipeMarket.domain.entity.QOrderItem.orderItem;
import static _4.NovemberRecipeMarket.domain.entity.QUser.user;

@Repository
@RequiredArgsConstructor
public class OrderCustomRepositoryImpl implements OrderCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Order> searchOrder(OrderSearch orderSearch, String username, Pageable pageable) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        List<Order> contents = queryFactory
                .select(order)
                .from(order)
                .join(order.user, user).fetchJoin()
                .join(order.orderedItemList, orderItem).fetchJoin()
                .join(orderItem.item, item).fetchJoin()
                .where(usernameEq(username),
                        orderStatusEq(orderSearch.getOrderStatus()),
                        itemNameEq(orderSearch.getItemName()))
                .orderBy(order.orderedDate.desc())
                .distinct()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory
                .select(order.count())
                .from(order)
                .join(order.user, user)
                .join(order.orderedItemList, orderItem)
                .join(orderItem.item, item)
                .where(usernameEq(username),
                        orderStatusEq(orderSearch.getOrderStatus()),
                        itemNameEq(orderSearch.getItemName()))
                .distinct()
                .fetchOne();

        return new PageImpl<>(contents, pageable, total);
    }

    private BooleanExpression usernameEq(String username) {
        return StringUtils.hasText(username) ? user.username.eq(username) : null;
    }

    private BooleanExpression orderStatusEq(OrderStatus orderStatus) {
        return orderStatus != null ? order.orderStatus.eq(orderStatus) : null;
    }

    private BooleanExpression itemNameEq(String itemName) {
        return !StringUtils.hasText(itemName) ? item.itemName.eq(itemName) : null;
    }
}
