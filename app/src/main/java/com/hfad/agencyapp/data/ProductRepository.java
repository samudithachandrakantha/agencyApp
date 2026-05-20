package com.hfad.agencyapp.data;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.hfad.agencyapp.data.dao.ProductDao;
import com.hfad.agencyapp.data.entities.Category;
import com.hfad.agencyapp.data.entities.Product;

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
                        new Category("General", "Default category for products"),
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

    public void shutdown() {
        executor.shutdown();
    }
}
