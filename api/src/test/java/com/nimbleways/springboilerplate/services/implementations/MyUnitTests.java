package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@UnitTest
public class MyUnitTests {

    @Mock
    private NotificationService notificationService;
    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private ProductService productService;

    // ── NORMAL ────────────────────────────────────────────────────────────────

    @Test
    public void normal_withStock_decrementsAvailable() {
        Product product = new Product(null, 15, 30, "NORMAL", "USB Cable", null, null, null);
        when(productRepository.save(product)).thenReturn(product);

        productService.processProduct(product);

        assertEquals(29, product.getAvailable());
        verify(productRepository).save(product);
        verifyNoInteractions(notificationService);
    }

    @Test
    public void normal_outOfStock_notifiesDelay() {
        Product product = new Product(null, 15, 0, "NORMAL", "USB Dongle", null, null, null);
        when(productRepository.save(product)).thenReturn(product);

        productService.processProduct(product);

        verify(notificationService).sendDelayNotification(15, "USB Dongle");
    }

    @Test
    public void normal_outOfStockNoLeadTime_doesNotNotify() {
        Product product = new Product(null, 0, 0, "NORMAL", "Unknown Item", null, null, null);

        productService.processProduct(product);

        verifyNoInteractions(notificationService);
        verifyNoInteractions(productRepository);
    }

    @Test
    public void notifyDelay_updatesLeadTimeAndNotifies() {
        Product product = new Product(null, 15, 0, "NORMAL", "RJ45 Cable", null, null, null);
        Mockito.when(productRepository.save(product)).thenReturn(product);

        productService.notifyDelay(product.getLeadTime(), product);

        assertEquals(0, product.getAvailable());
        assertEquals(15, product.getLeadTime());
        verify(productRepository, times(1)).save(product);
        verify(notificationService, times(1)).sendDelayNotification(15, "RJ45 Cable");
    }

    // ── SEASONAL ──────────────────────────────────────────────────────────────

    @Test
    public void seasonal_inSeasonWithStock_decrementsAvailable() {
        Product product = new Product(null, 15, 30, "SEASONAL", "Watermelon", null,
                LocalDate.now().minusDays(10), LocalDate.now().plusDays(58));
        when(productRepository.save(product)).thenReturn(product);

        productService.processProduct(product);

        assertEquals(29, product.getAvailable());
        verifyNoInteractions(notificationService);
    }

    @Test
    public void seasonal_seasonEnded_notifiesOutOfStockAndSetsZero() {
        Product product = new Product(null, 15, 30, "SEASONAL", "Watermelon", null,
                LocalDate.now().minusDays(30), LocalDate.now().minusDays(2));
        when(productRepository.save(product)).thenReturn(product);

        productService.processProduct(product);

        verify(notificationService).sendOutOfStockNotification("Watermelon");
        assertEquals(0, product.getAvailable());
    }

    @Test
    public void seasonal_beforeSeason_notifiesOutOfStock() {
        Product product = new Product(null, 15, 30, "SEASONAL", "Grapes", null,
                LocalDate.now().plusDays(180), LocalDate.now().plusDays(240));
        when(productRepository.save(product)).thenReturn(product);

        productService.processProduct(product);

        verify(notificationService).sendOutOfStockNotification("Grapes");
    }

    @Test
    public void seasonal_inSeasonOutOfStock_leadTimeWithinSeason_notifiesDelay() {
        Product product = new Product(null, 5, 0, "SEASONAL", "Mango", null,
                LocalDate.now().minusDays(10), LocalDate.now().plusDays(58));
        when(productRepository.save(product)).thenReturn(product);

        productService.processProduct(product);

        verify(notificationService).sendDelayNotification(5, "Mango");
    }

    // ── EXPIRABLE ─────────────────────────────────────────────────────────────

    @Test
    public void expirable_notExpiredWithStock_decrementsAvailable() {
        Product product = new Product(null, 15, 30, "EXPIRABLE", "Butter",
                LocalDate.now().plusDays(26), null, null);
        when(productRepository.save(product)).thenReturn(product);

        productService.processProduct(product);

        assertEquals(29, product.getAvailable());
        verifyNoInteractions(notificationService);
    }

    @Test
    public void expirable_expired_notifiesExpirationAndSetsZero() {
        LocalDate expiryDate = LocalDate.now().minusDays(2);
        Product product = new Product(null, 90, 6, "EXPIRABLE", "Milk", expiryDate, null, null);
        when(productRepository.save(product)).thenReturn(product);

        productService.processProduct(product);

        verify(notificationService).sendExpirationNotification("Milk", expiryDate);
        assertEquals(0, product.getAvailable());
    }

    @Test
    public void expirable_notExpiredOutOfStock_notifiesExpiration() {
        LocalDate expiryDate = LocalDate.now().plusDays(10);
        Product product = new Product(null, 15, 0, "EXPIRABLE", "Cheese", expiryDate, null, null);
        when(productRepository.save(product)).thenReturn(product);

        productService.processProduct(product);

        verify(notificationService).sendExpirationNotification("Cheese", expiryDate);
        assertEquals(0, product.getAvailable());
    }
}
