package fr.shoploc.shoplocBackend.productInCart.service;

import fr.shoploc.shoplocBackend.common.models.Product;
import fr.shoploc.shoplocBackend.common.models.ProductInCart;
import fr.shoploc.shoplocBackend.config.JwtService;
import fr.shoploc.shoplocBackend.productInCart.repository.ProductInCartRepository;
import fr.shoploc.shoplocBackend.product.controller.ProductController;
import fr.shoploc.shoplocBackend.usermanager.user.User;
import fr.shoploc.shoplocBackend.usermanager.user.UserService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProductInCartService {

    private final ProductInCartRepository productInCartRepository;
    private final ProductController productController;
    private final JwtService jwtService;
    private final UserService userService;

    public ProductInCartService(ProductInCartRepository productInCartRepository, ProductController productController, JwtService jwtService, UserService userService){
        this.productInCartRepository = productInCartRepository;
        this.productController = productController;
        this.jwtService = jwtService;
        this.userService = userService;
    }
    public void addProductToCart(Long idProduct, String token) throws Exception {
        Long userId = getUserId(token);
        ProductInCart productInCart = new ProductInCart(idProduct, userId);
        productInCartRepository.save(productInCart);
    }

    public void removeProductToCart(Long idProduct, String token) throws Exception {
        Long userId = getUserId(token);
        productInCartRepository.deleteByProductIdAndUserId(idProduct, userId);
    }

    public Map<Long, List<HashMap<Product, Integer>>> getCarts(String token) throws Exception {
        Long userId = getUserId(token);

        List<ProductInCart> productInCartList = productInCartRepository.findAllByIdUser(userId);

        Map<Long, List<HashMap<Product, Integer>>> cartsByShop = new HashMap<>();

        for (ProductInCart productInCart : productInCartList) {
            Product product = productController.getProductById(productInCart.getIdProduct());
            Long shopId = product.getIdMagasin();

            HashMap<Product, Integer> productsWithQuantity = new HashMap<>();
            productsWithQuantity.put(product, productInCart.getQuantity());

            List<HashMap<Product, Integer>> shopCarts = cartsByShop.computeIfAbsent(shopId, k -> new ArrayList<>());

            shopCarts.add(productsWithQuantity);
        }

        return cartsByShop;
    }


    public Long getUserId(String token) throws Exception {
        String userEmail = jwtService.extractUserEmail(token);
        Optional<User> user = userService.findUserByEmail(userEmail);
        if (user.isPresent()) {
            return user.get().getId();
        } else {
            throw new Exception("Utilisateur introuvable.");
        }
    }
}
