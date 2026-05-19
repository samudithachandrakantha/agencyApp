package com.hfad.agencyapp.data;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.hfad.agencyapp.data.dao.ProductDao;
import com.hfad.agencyapp.data.entities.Category;
import com.hfad.agencyapp.data.entities.Product;
import com.hfad.agencyapp.data.entities.StockMovement;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ProductRepository {
    private final AppDatabase db;
    private final ProductDao productDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ProductRepository(Context context) {
        db = AppDatabase.getInstance(context.getApplicationContext());
        productDao = db.productDao();
    }

    public LiveData<List<Product>> getAllProducts() {
        return productDao.getAll();
    }

    public Future<List<Category>> getAllCategoriesAsync() {
        return executor.submit(new Callable<List<Category>>() {
            @Override
            public List<Category> call() {
                return db.categoryDao().getAll();
            }
        });
    }

    public Future<Void> ensureDefaultCategories() {
        return executor.submit(() -> {
            List<Category> categories = db.categoryDao().getAll();
            if (categories == null || categories.isEmpty()) {
                List<Category> defaults = Arrays.asList(
                        new Category("Flour & Grains", "Flour and grain products"),
                        new Category("Sugars", "Sugar and sweeteners"),
                        new Category("Food Colors", "Food color products"),
                        new Category("Dairy & Fats", "Butter, ghee and fats"),
                        new Category("Baking Tools", "Bakeware and tools"),
                        new Category("Packaging", "Packaging materials"),
                        new Category("Decorations", "Decoration items")
                );
                for (Category category : defaults) {
                    db.categoryDao().insert(category);
                }
            }
            return null;
        });
    }

    public Future<Long> insertOrUpdateAsync(final Product product) {
        return executor.submit(() -> {
            if (product.id == 0) {
                long id = productDao.insert(product);
                product.id = id;
                return id;
            } else {
                product.updatedAt = System.currentTimeMillis();
                productDao.update(product);
                return product.id;
            }
        });
    }

    public Future<Boolean> deleteAsync(final long productId) {
        return executor.submit(() -> {
            Product product = productDao.getById(productId);
            if (product == null) return false;
            return productDao.delete(product) > 0;
        });
    }

    public Future<Boolean> deleteByIdAsync(final long productId) {
        return executor.submit(() -> productDao.deleteById(productId) > 0);
    }

    public Future<Product> getByIdAsync(final long productId) {
        return executor.submit(() -> productDao.getById(productId));
    }

    public Future<List<Product>> searchAsync(final String query) {
        return executor.submit(() -> productDao.search("%" + query + "%"));
    }

    public Future<List<Product>> getLowStockAsync() {
        return executor.submit(productDao::getLowStock);
    }

    public Future<List<Product>> getOutOfStockAsync() {
        return executor.submit(productDao::getOutOfStock);
    }

    public Future<List<Product>> getByCategoryAsync(final long categoryId) {
        return executor.submit(() -> productDao.getByCategoryId(categoryId));
    }

    public Future<Boolean> adjustStockAsync(final long productId, final boolean add, final int quantity, final String reason, final String notes) {
        return executor.submit(() -> {
            Product product = productDao.getById(productId);
            if (product == null || quantity <= 0) return false;

            int newStock = add ? product.stock + quantity : product.stock - quantity;
            if (newStock < 0) return false;

            long updatedAt = System.currentTimeMillis();
            productDao.updateStock(productId, newStock, updatedAt);
            db.stockMovementDao().insert(new StockMovement(productId, add ? "IN" : "OUT", quantity, reason, notes));
            return true;
        });
    }

    public Future<List<StockMovement>> getRecentMovementsAsync(final long productId) {
        return executor.submit(() -> db.stockMovementDao().getRecentByProduct(productId, 5));
    }

    public void shutdown() {
        executor.shutdown();
    }
}



