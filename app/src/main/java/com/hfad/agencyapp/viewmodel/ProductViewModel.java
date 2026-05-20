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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ProductViewModel extends AndroidViewModel {

    public enum StockFilter { ALL, LOW_STOCK, OUT_OF_STOCK }

    private final ProductRepository repository;
    private final LiveData<List<Product>> allProducts;
    private final MediatorLiveData<List<Product>> products = new MediatorLiveData<>();
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
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
        products.addSource(stockFilter, sf -> applyFilters());
    }

    public LiveData<List<Product>> getProducts() { return products; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }
    public LiveData<String> getSearchQuery() { return searchQuery; }
    public LiveData<StockFilter> getStockFilter() { return stockFilter; }

    public void setSearchQuery(String q) { searchQuery.setValue(q == null ? "" : q); }
    public void setStockFilter(StockFilter filter) {
        stockFilter.setValue(filter == null ? StockFilter.ALL : filter);
    }

    private void applyFilters() {
        List<Product> filtered = new ArrayList<>(cache);
        String q = searchQuery.getValue();
        StockFilter sf = stockFilter.getValue();

        if (q != null && !q.trim().isEmpty()) {
            String lower = q.toLowerCase();
            List<Product> tmp = new ArrayList<>();
            for (Product p : filtered) {
                if ((p.name != null && p.name.toLowerCase().contains(lower)) ||
                        (p.sku != null && p.sku.toLowerCase().contains(lower)) ||
                        (p.brand != null && p.brand.toLowerCase().contains(lower))) {
                    tmp.add(p);
                }
            }
            filtered = tmp;
        }

        if (sf != null && sf != StockFilter.ALL) {
            List<Product> tmp = new ArrayList<>();
            for (Product p : filtered) {
                if (sf == StockFilter.OUT_OF_STOCK && p.stock == 0) {
                    tmp.add(p);
                } else if (sf == StockFilter.LOW_STOCK && p.stock > 0 && p.stock < p.lowStockThreshold) {
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

    /**
     * Resolves category for products created without category UI (prefers "General").
     * Call from a background thread.
     */
    public long resolveDefaultCategoryId() {
        try {
            repository.ensureDefaultCategories().get();
            List<Category> cats = repository.getAllCategoriesAsync().get();
            if (cats != null) {
                for (Category c : cats) {
                    if ("General".equals(c.name)) {
                        return c.id;
                    }
                }
                if (!cats.isEmpty()) {
                    return cats.get(0).id;
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            error.postValue(e.getMessage());
        }
        return 1L;
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

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.shutdown();
    }
}
