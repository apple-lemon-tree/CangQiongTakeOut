package com.abc.service;

import com.abc.dto.ShoppingCartDTO;
import com.abc.entity.ShoppingCartItem;

import java.util.List;

public interface ShoppingCartService {
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    List<ShoppingCartItem> showShoppingCart();

    void cleanShoppingCart();

    void subShoppingCart(ShoppingCartDTO shoppingCartDTO);
}
