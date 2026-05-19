package com.hfad.agencyapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.hfad.agencyapp.data.ProductRepository;
import com.hfad.agencyapp.data.entities.Category;
import com.hfad.agencyapp.data.entities.Product;
import com.hfad.agencyapp.data.entities.StockMovement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ProductViewModel extends AndroidViewModel {

    public enum StockFilter { ALL, LOW_STOCK, OUT_OF_STOCK }

    private final ProductRepository repository;
    private final LiveData<List<Product>> allProducts;
    private final MediatorLiveData<List<Product>> products = new MediatorLiveData<>();
    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<Long> categoryFilter = new MutableLiveData<>(-1L);
    private final MutableLiveData<StockFilter> stockFilter = new MutableLiveData<>(StockFilter.ALL);
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);

    private List<Product> cache = new ArrayList<>();

    public ProductViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductRepository(application);
        allProducts = repository.getAllProducts();

        products.addSource(allProducts, list -> {
            cache = list != null ? list : new ArrayList<>();
            applyFilters();
        });
        products.addSource(searchQuery, s -> applyFilters());
        products.addSource(categoryFilter, id -> applyFilters());
        products.addSource(stockFilter, sf -> applyFilters());

        loadCategories();
    }

    public LiveData<List<Product>> getProducts() { return products; }
    public LiveData<List<Category>> getCategories() { return categories; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }
    public LiveData<String> getSearchQuery() { return searchQuery; }
    public LiveData<Long> getCategoryFilter() { return categoryFilter; }
    public LiveData<StockFilter> getStockFilter() { return stockFilter; }

    public void setSearchQuery(String q) { searchQuery.setValue(q == null ? "" : q); }
    public void setCategoryFilter(long id) { categoryFilter.setValue(id); }
    public void setStockFilter(StockFilter filter) { stockFilter.setValue(filter == null ? StockFilter.ALL : filter); }

    public void resetFilters() {
        searchQuery.setValue("");
        categoryFilter.setValue(-1L);
        stockFilter.setValue(StockFilter.ALL);
    }

    public void loadCategories() {
        loading.postValue(true);
        new Thread(() -> {
            try {
                repository.ensureDefaultCategories().get();
                Future<List<Category>> future = repository.getAllCategoriesAsync();
                categories.postValue(future.get());
            } catch (ExecutionException | InterruptedException e) {
                error.postValue(e.getMessage());
            } finally {
                loading.postValue(false);
            }
        }).start();
    }

    private void applyFilters() {
        List<Product> filtered = new ArrayList<>(cache);
        String q = searchQuery.getValue();
        Long catId = categoryFilter.getValue();
        StockFilter sf = stockFilter.getValue();

        if (q != null && !q.trim().isEmpty()) {
            String lower = q.toLowerCase();
            List<Product> tmp = new ArrayList<>();
            for (Product p : filtered) {
                if ((p.name != null && p.name.toLowerCase().contains(lower)) ||
                        (p.sku != null && p.sku.toLowerCase().contains(lower))) {
                    tmp.add(p);
                }
            }
            filtered = tmp;
        }

        if (catId != null && catId > 0) {
            List<Product> tmp = new ArrayList<>();
            for (Product p : filtered) {
                if (p.categoryId == catId) tmp.add(p);
            }
            filtered = tmp;
        }

        if (sf != null) {
            List<Product> tmp = new ArrayList<>();
            for (Product p : filtered) {
                boolean low = p.stock <= 0 || p.stock < p.lowStockThreshold;
                if (sf == StockFilter.ALL || (sf == StockFilter.LOW_STOCK && low) || (sf == StockFilter.OUT_OF_STOCK && p.stock == 0)) {
                    tmp.add(p);
                }
            }
            filtered = tmp;
        }

        products.setValue(filtered);
    }

    public boolean saveProduct(Product product) {
        try {
            Future<Long> future = repository.insertOrUpdateAsync(product);
            return future.get() > 0;
        } catch (ExecutionException | InterruptedException e) {
            error.postValue(e.getMessage());
            return false;
        }
    }

    public boolean deleteProduct(long id) {
        try {
            Future<Boolean> future = repository.deleteAsync(id);
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            error.postValue(e.getMessage());
            return false;
        }
    }

    public Product getById(long id) {
        try {
            Future<Product> future = repository.getByIdAsync(id);
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            error.postValue(e.getMessage());
            return null;
        }
    }

    public List<StockMovement> getRecentMovements(long productId) {
        try {
            Future<List<StockMovement>> future = repository.getRecentMovementsAsync(productId);
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            error.postValue(e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean adjustStock(long productId, boolean add, int qty, String reason, String notes) {
        try {
            Future<Boolean> future = repository.adjustStockAsync(productId, add, qty, reason, notes);
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            error.postValue(e.getMessage());
            return false;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.shutdown();
    }
}



