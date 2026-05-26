package com.hfad.agencyapp.ui.insights;

import android.content.Context;
import android.graphics.Color;

import androidx.lifecycle.LifecycleOwner;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.hfad.agencyapp.R;
import com.hfad.agencyapp.data.Repository;
import com.hfad.agencyapp.data.entities.InvoiceItem;
import com.hfad.agencyapp.data.entities.Customer;
import com.hfad.agencyapp.data.entities.Invoice;
import com.hfad.agencyapp.data.entities.Product;
import com.hfad.agencyapp.data.entities.Category;
import com.hfad.agencyapp.databinding.ActivityInsightsBinding;
import com.hfad.agencyapp.viewmodel.DashboardViewModel;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

public class InsightsDashboardBinder {

    public enum PeriodFilter {
        TODAY,
        YESTERDAY,
        LAST_7_DAYS,
        LAST_30_DAYS,
        CUSTOM,
        ALL_TIME
    }

    public enum BreakdownFilter {
        PAYMENT_METHOD,
        INVOICE_STATUS
    }

    private final ActivityInsightsBinding binding;
    private final DashboardViewModel viewModel;
    private final LifecycleOwner owner;
    private final Context context;
    private final Repository repository;

    private final DecimalFormat moneyFormat = new DecimalFormat("#,##0.00");
    private final SimpleDateFormat dayLabelFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());
    private final SimpleDateFormat monthLabelFormat = new SimpleDateFormat("MMM yy", Locale.getDefault());
    private final SimpleDateFormat shortDayLabelFormat = new SimpleDateFormat("EEE", Locale.getDefault());

    private final List<Invoice> invoices = new ArrayList<>();
    private final List<Customer> customers = new ArrayList<>();
    private final List<Product> products = new ArrayList<>();
    private final List<Category> categories = new ArrayList<>();

    private final AtomicInteger topProductsRenderToken = new AtomicInteger(0);
    private final AtomicInteger categoriesRenderToken = new AtomicInteger(0);
    private final AtomicInteger profitRenderToken = new AtomicInteger(0);

    private PeriodFilter periodFilter = PeriodFilter.LAST_7_DAYS;
    private BreakdownFilter breakdownFilter = BreakdownFilter.PAYMENT_METHOD;
    
    private long customStartDate = 0L;
    private long customEndDate = 0L;

    public InsightsDashboardBinder(ActivityInsightsBinding binding,
                                   DashboardViewModel viewModel,
                                   LifecycleOwner owner,
                                   Context context) {
        this.binding = binding;
        this.viewModel = viewModel;
        this.owner = owner;
        this.context = context;
        this.repository = Repository.getInstance(context);
    }

    public void attach() {
        setupChips();
        setupCharts();
        observeData();
        render();
    }

    private void setupChips() {
        binding.chipGroupPeriod.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip_custom) {
                showDateRangePicker();
            } else {
                periodFilter = resolvePeriodFilter(checkedIds);
                customStartDate = 0L;
                customEndDate = 0L;
                render();
            }
        });

        binding.chipGroupBreakdown.setOnCheckedStateChangeListener((group, checkedIds) -> {
            breakdownFilter = resolveBreakdownFilter(checkedIds);
            render();
        });
    }
    
    private void showDateRangePicker() {
        MaterialDatePicker<androidx.core.util.Pair<Long, Long>> datePicker = MaterialDatePicker.Builder.dateRangePicker()
                .build();
        
        datePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<androidx.core.util.Pair<Long, Long>>() {
            @Override
            public void onPositiveButtonClick(androidx.core.util.Pair<Long, Long> selection) {
                if (selection != null) {
                    customStartDate = selection.first;
                    customEndDate = selection.second;
                    periodFilter = PeriodFilter.CUSTOM;
                    render();
                }
            }
        });
        
        datePicker.addOnNegativeButtonClickListener(dialog -> {
            // If user cancels, revert to the previous selection
            binding.chipGroupPeriod.clearCheck();
            periodFilter = PeriodFilter.LAST_7_DAYS;
        });
        
        if (binding.getRoot().getContext() instanceof androidx.appcompat.app.AppCompatActivity) {
            datePicker.show(((androidx.appcompat.app.AppCompatActivity) binding.getRoot().getContext())
                    .getSupportFragmentManager(), "date_range_picker");
        }
    }

    private void observeData() {
        viewModel.invoices.observe(owner, list -> {
            invoices.clear();
            if (list != null) {
                invoices.addAll(list);
            }
            render();
        });

        viewModel.customers.observe(owner, list -> {
            customers.clear();
            if (list != null) {
                customers.addAll(list);
            }
            render();
        });

        viewModel.products.observe(owner, list -> {
            products.clear();
            if (list != null) {
                products.addAll(list);
            }
            render();
        });

        viewModel.categories.observe(owner, list -> {
            categories.clear();
            if (list != null) {
                categories.addAll(list);
            }
            render();
        });
    }

    private void setupCharts() {
        configureBarChart(binding.barChartRevenue);
        configureHorizontalBarChart(binding.barChartTopProducts);
        configurePieChart(binding.pieChartBreakdown);
        configureBarChart(binding.barChartByCategory);
        configureHorizontalBarChart(binding.barChartCustomerRanking);
    }

    private void render() {
        List<Invoice> selectedInvoices = filterInvoicesByPeriod(invoices, periodFilter);
        List<Invoice> comparisonInvoices = filterComparisonInvoices(invoices, periodFilter);

        double revenue = sumRevenue(selectedInvoices);
        int orderCount = selectedInvoices.size();
        int customerCount = countDistinctCustomers(selectedInvoices);
        int productCount = products.size();
        double averageOrder = orderCount > 0 ? revenue / orderCount : 0.0;

        binding.tvInsightSales.setText(context.getString(R.string.amount_format, moneyFormat.format(revenue)));
        binding.tvInsightInvoices.setText(String.valueOf(orderCount));
        binding.tvInsightCustomers.setText(String.valueOf(customerCount));
        binding.tvInsightProducts.setText(String.valueOf(productCount));

        renderProfit(selectedInvoices);
        renderBarChart(selectedInvoices);
        renderPieChart(selectedInvoices);
        renderTopProductsChart(selectedInvoices);
        renderCategoryChart(selectedInvoices);
        renderCustomerRankingChart(selectedInvoices);
        renderSummary(selectedInvoices, comparisonInvoices, revenue, orderCount, averageOrder);
    }

    private void renderCategoryChart(List<Invoice> selectedInvoices) {
        final int requestToken = categoriesRenderToken.incrementAndGet();
        final List<Invoice> snapshot = new ArrayList<>(selectedInvoices);

        new Thread(() -> {
            Map<Long, Double> byCategory = new HashMap<>();

            for (Invoice invoice : snapshot) {
                if (invoice == null) continue;
                List<InvoiceItem> items = repository.getInvoiceItemsOnce(invoice.id);
                if (items == null) continue;
                for (InvoiceItem item : items) {
                    Product p = findProductById(item.productId);
                    long catId = p != null ? p.categoryId : -1L;
                    double sale = item.totalPrice > 0.0 ? item.totalPrice : (item.quantity * item.unitPrice);
                    byCategory.put(catId, byCategory.getOrDefault(catId, 0.0) + sale);
                }
            }

            List<Map.Entry<Long, Double>> entriesList = new ArrayList<>(byCategory.entrySet());
            Collections.sort(entriesList, (a, b) -> Double.compare(b.getValue(), a.getValue()));

            final List<BarEntry> entries = new ArrayList<>();
            final List<String> labels = new ArrayList<>();
            for (int i = 0; i < entriesList.size(); i++) {
                Map.Entry<Long, Double> e = entriesList.get(i);
                entries.add(new BarEntry(i, e.getValue().floatValue()));
                labels.add(resolveCategoryName(e.getKey()));
            }

            binding.getRoot().post(() -> {
                if (requestToken != categoriesRenderToken.get()) return;
                if (entries.isEmpty()) {
                    binding.barChartByCategory.clear();
                    binding.barChartByCategory.setNoDataText("No category sales data for this period");
                    binding.barChartByCategory.invalidate();
                    return;
                }

                BarDataSet dataSet = new BarDataSet(entries, "Category Sales");
                dataSet.setColor(context.getColor(R.color.soft_blue_icon));
                dataSet.setValueTextColor(context.getColor(R.color.text_primary));
                dataSet.setValueTextSize(10f);

                BarData data = new BarData(dataSet);
                data.setBarWidth(0.7f);
                data.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getBarLabel(com.github.mikephil.charting.data.BarEntry barEntry) {
                        return moneyFormat.format(barEntry.getY());
                    }
                });

                binding.barChartByCategory.setData(data);
                binding.barChartByCategory.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
                binding.barChartByCategory.getAxisRight().setEnabled(false);
                binding.barChartByCategory.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                binding.barChartByCategory.getXAxis().setGranularity(1f);
                binding.barChartByCategory.getXAxis().setDrawGridLines(false);
                binding.barChartByCategory.getAxisLeft().setAxisMinimum(0f);
                binding.barChartByCategory.getDescription().setEnabled(false);
                binding.barChartByCategory.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
                binding.barChartByCategory.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
                binding.barChartByCategory.animateY(600);
                binding.barChartByCategory.invalidate();
            });
        }).start();
    }

    private void renderCustomerRankingChart(List<Invoice> selectedInvoices) {
        Map<String, Double> byCustomer = new HashMap<>();
        for (Invoice invoice : selectedInvoices) {
            String name = safeValue(invoice.customerName);
            double sale = invoice.totalAmount;
            byCustomer.put(name, byCustomer.getOrDefault(name, 0.0) + sale);
        }

        List<Map.Entry<String, Double>> ranking = new ArrayList<>(byCustomer.entrySet());
        Collections.sort(ranking, (l, r) -> Double.compare(r.getValue(), l.getValue()));
        if (ranking.isEmpty()) {
            binding.barChartCustomerRanking.clear();
            binding.barChartCustomerRanking.setNoDataText("No customer data for this period");
            binding.barChartCustomerRanking.invalidate();
            return;
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        int limit = Math.min(6, ranking.size());
        for (int i = 0; i < limit; i++) {
            Map.Entry<String, Double> e = ranking.get(i);
            entries.add(new BarEntry(i, e.getValue().floatValue()));
            labels.add(shortenLabel(e.getKey()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Customer Revenue");
        dataSet.setColor(context.getColor(R.color.navy_900));
        dataSet.setValueTextColor(context.getColor(R.color.text_primary));
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.7f);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getBarLabel(com.github.mikephil.charting.data.BarEntry barEntry) {
                return moneyFormat.format(barEntry.getY());
            }
        });

        binding.barChartCustomerRanking.setData(data);
        binding.barChartCustomerRanking.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        binding.barChartCustomerRanking.getAxisLeft().setDrawGridLines(false);
        binding.barChartCustomerRanking.getAxisLeft().setAxisMinimum(0f);
        binding.barChartCustomerRanking.getAxisRight().setEnabled(false);
        binding.barChartCustomerRanking.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        binding.barChartCustomerRanking.getXAxis().setGranularity(1f);
        binding.barChartCustomerRanking.getXAxis().setDrawGridLines(false);
        binding.barChartCustomerRanking.getDescription().setEnabled(false);
        binding.barChartCustomerRanking.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        binding.barChartCustomerRanking.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        binding.barChartCustomerRanking.animateY(650);
        binding.barChartCustomerRanking.invalidate();
    }

    private Product findProductById(long id) {
        for (Product p : products) {
            if (p != null && p.id == id) return p;
        }
        return null;
    }

    private String resolveCategoryName(long id) {
        if (id <= 0) return "Uncategorized";
        for (Category c : categories) {
            if (c != null && c.id == id) return safeValue(c.name);
        }
        return "Category #" + id;
    }

    private void renderSummary(List<Invoice> selectedInvoices,
                               List<Invoice> comparisonInvoices,
                               double revenue,
                               int orderCount,
                               double averageOrder) {
        // Summary card removed from layout; only update breakdown title now.
        binding.tvBreakdownTitle.setText(breakdownFilter == BreakdownFilter.PAYMENT_METHOD
            ? "Payment method mix"
            : "Invoice status mix");
    }

    private void renderBarChart(List<Invoice> selectedInvoices) {
        List<BuckeTable> buckets = buildBuckets(selectedInvoices, periodFilter);
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        for (int i = 0; i < buckets.size(); i++) {
            BuckeTable bucket = buckets.get(i);
            entries.add(new BarEntry(i, (float) bucket.revenue));
            labels.add(bucket.label);
        }

        if (entries.isEmpty()) {
            binding.barChartRevenue.clear();
            binding.barChartRevenue.setNoDataText("No revenue data for this period");
            binding.barChartRevenue.invalidate();
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Revenue");
        dataSet.setColor(context.getColor(R.color.navy_900));
        dataSet.setDrawValues(true);
        dataSet.setValueTextColor(context.getColor(R.color.text_primary));
        dataSet.setValueTextSize(10f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getBarLabel(com.github.mikephil.charting.data.BarEntry barEntry) {
                return moneyFormat.format(barEntry.getY());
            }
        });

        binding.barChartRevenue.setData(data);
        binding.barChartRevenue.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        binding.barChartRevenue.getAxisRight().setEnabled(false);
        binding.barChartRevenue.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        binding.barChartRevenue.getXAxis().setGranularity(1f);
        binding.barChartRevenue.getXAxis().setDrawGridLines(false);
        binding.barChartRevenue.getAxisLeft().setDrawGridLines(true);
        binding.barChartRevenue.getAxisLeft().setAxisMinimum(0f);
        binding.barChartRevenue.getDescription().setEnabled(false);
        binding.barChartRevenue.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        binding.barChartRevenue.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        binding.barChartRevenue.animateY(600);
        binding.barChartRevenue.invalidate();
    }

    private void renderPieChart(List<Invoice> selectedInvoices) {
        Map<String, Double> revenueBuckets = new LinkedHashMap<>();
        Map<String, Double> countBuckets = new LinkedHashMap<>();

        for (Invoice invoice : selectedInvoices) {
            String key = breakdownFilter == BreakdownFilter.PAYMENT_METHOD
                    ? normalizePaymentMethod(invoice.paymentMethod)
                    : normalizeStatus(invoice.status);
            double value = breakdownFilter == BreakdownFilter.PAYMENT_METHOD
                    ? invoice.totalAmount
                    : 1.0;

            if (breakdownFilter == BreakdownFilter.PAYMENT_METHOD) {
                revenueBuckets.put(key, revenueBuckets.getOrDefault(key, 0.0) + value);
            } else {
                countBuckets.put(key, countBuckets.getOrDefault(key, 0.0) + value);
            }
        }

        List<PieEntry> entries = new ArrayList<>();
        if (breakdownFilter == BreakdownFilter.PAYMENT_METHOD) {
            for (Map.Entry<String, Double> entry : revenueBuckets.entrySet()) {
                entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
            }
        } else {
            for (Map.Entry<String, Double> entry : countBuckets.entrySet()) {
                entries.add(new PieEntry(entry.getValue().floatValue(), entry.getKey()));
            }
        }

        if (entries.isEmpty()) {
            binding.pieChartBreakdown.clear();
            binding.pieChartBreakdown.setNoDataText("No breakdown data for this period");
            binding.pieChartBreakdown.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, breakdownFilter == BreakdownFilter.PAYMENT_METHOD ? "Revenue" : "Invoices");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(11f);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPieLabel(float value, PieEntry pieEntry) {
                if (breakdownFilter == BreakdownFilter.PAYMENT_METHOD) {
                    return moneyFormat.format(value);
                }
                return String.valueOf((int) value);
            }
        });

        PieData data = new PieData(dataSet);
        binding.pieChartBreakdown.setData(data);
        binding.pieChartBreakdown.setUsePercentValues(false);
        binding.pieChartBreakdown.setDrawEntryLabels(true);
        binding.pieChartBreakdown.setEntryLabelColor(context.getColor(R.color.text_primary));
        binding.pieChartBreakdown.setEntryLabelTextSize(11f);
        binding.pieChartBreakdown.setHoleColor(context.getColor(R.color.dashboard_bg));
        binding.pieChartBreakdown.getDescription().setEnabled(false);
        binding.pieChartBreakdown.setDrawHoleEnabled(true);
        binding.pieChartBreakdown.setHoleRadius(55f);
        binding.pieChartBreakdown.setTransparentCircleRadius(60f);
        binding.pieChartBreakdown.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        binding.pieChartBreakdown.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        binding.pieChartBreakdown.animateY(700);
        binding.pieChartBreakdown.invalidate();
    }

    private void renderTopProductsChart(List<Invoice> selectedInvoices) {
        final int requestToken = topProductsRenderToken.incrementAndGet();
        final List<Invoice> snapshot = new ArrayList<>(selectedInvoices);

        new Thread(() -> {
            Map<String, ProductStats> topProducts = new HashMap<>();
            for (Invoice invoice : snapshot) {
                if (invoice == null) {
                    continue;
                }
                List<InvoiceItem> items = repository.getInvoiceItemsOnce(invoice.id);
                if (items == null) {
                    continue;
                }
                for (InvoiceItem item : items) {
                    String productName = resolveProductName(item.productId);
                    ProductStats stats = topProducts.get(productName);
                    if (stats == null) {
                        stats = new ProductStats(productName);
                        topProducts.put(productName, stats);
                    }
                    double saleValue = item.totalPrice > 0.0 ? item.totalPrice : (item.quantity * item.unitPrice);
                    stats.revenue += saleValue;
                    stats.units += item.quantity;
                }
            }

            List<ProductStats> ranking = new ArrayList<>(topProducts.values());
            Collections.sort(ranking, (left, right) -> Double.compare(right.revenue, left.revenue));
            if (ranking.size() > 5) {
                ranking = ranking.subList(0, 5);
            }

            List<BarEntry> entries = new ArrayList<>();
            List<String> labels = new ArrayList<>();
            for (int i = 0; i < ranking.size(); i++) {
                ProductStats stats = ranking.get(i);
                entries.add(new BarEntry(i, (float) stats.revenue));
                labels.add(shortenLabel(stats.name));
            }

            final List<ProductStats> finalRanking = ranking;
            binding.getRoot().post(() -> {
                if (requestToken != topProductsRenderToken.get()) {
                    return;
                }
                if (entries.isEmpty()) {
                    binding.barChartTopProducts.clear();
                    binding.barChartTopProducts.setNoDataText("No product sales data for this period");
                    binding.barChartTopProducts.invalidate();
                    return;
                }

                BarDataSet dataSet = new BarDataSet(entries, "Revenue");
                dataSet.setColor(context.getColor(R.color.soft_blue_icon));
                dataSet.setValueTextColor(context.getColor(R.color.text_primary));
                dataSet.setValueTextSize(10f);

                BarData data = new BarData(dataSet);
                data.setBarWidth(0.7f);
                data.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getBarLabel(com.github.mikephil.charting.data.BarEntry barEntry) {
                        return moneyFormat.format(barEntry.getY());
                    }
                });

                binding.barChartTopProducts.setData(data);
                binding.barChartTopProducts.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
                binding.barChartTopProducts.getAxisLeft().setDrawGridLines(false);
                binding.barChartTopProducts.getAxisLeft().setAxisMinimum(0f);
                binding.barChartTopProducts.getAxisRight().setEnabled(false);
                binding.barChartTopProducts.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                binding.barChartTopProducts.getXAxis().setGranularity(1f);
                binding.barChartTopProducts.getXAxis().setDrawGridLines(false);
                binding.barChartTopProducts.getDescription().setEnabled(false);
                binding.barChartTopProducts.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
                binding.barChartTopProducts.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
                binding.barChartTopProducts.animateY(650);
                binding.barChartTopProducts.invalidate();

                if (!finalRanking.isEmpty()) {
                    ProductStats top = finalRanking.get(0);
                    binding.tvTopProductsTitle.setText("Top products - " + top.name);
                } else {
                    binding.tvTopProductsTitle.setText("Top products");
                }
            });
        }).start();
    }

    private void renderProfit(List<Invoice> selectedInvoices) {
        final int requestToken = profitRenderToken.incrementAndGet();
        final List<Invoice> snapshot = new ArrayList<>(selectedInvoices);

        new Thread(() -> {
            double profit = 0.0;
            for (Invoice invoice : snapshot) {
                if (invoice == null) {
                    continue;
                }
                List<InvoiceItem> items = repository.getInvoiceItemsOnce(invoice.id);
                if (items == null) {
                    continue;
                }
                for (InvoiceItem item : items) {
                    Product product = findProductById(item.productId);
                    double saleValue = item.totalPrice > 0.0 ? item.totalPrice : (item.quantity * item.unitPrice);
                    double unitCost = product != null ? product.costPrice : 0.0;
                    int costQuantity = item.quantity + Math.max(0, item.freeIssueUnits);
                    profit += saleValue - (unitCost * costQuantity);
                }
            }

            final double finalProfit = profit;
            binding.getRoot().post(() -> {
                if (requestToken != profitRenderToken.get()) {
                    return;
                }
                binding.tvInsightProfit.setText(context.getString(R.string.amount_format, moneyFormat.format(finalProfit)));
            });
        }).start();
    }

    private String resolveProductName(long productId) {
        for (Product product : products) {
            if (product != null && product.id == productId) {
                return safeValue(product.name);
            }
        }
        return "Product #" + productId;
    }

    private String shortenLabel(String value) {
        if (value == null) {
            return "Unknown";
        }
        String trimmed = value.trim();
        if (trimmed.length() <= 14) {
            return trimmed;
        }
        return trimmed.substring(0, 11) + "...";
    }

    private void configureBarChart(BarChart chart) {
        chart.setDrawGridBackground(false);
        chart.setFitBars(true);
        chart.setTouchEnabled(true);
        chart.setPinchZoom(true);
        chart.setScaleEnabled(true);
        chart.setExtraBottomOffset(4f);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setTextColor(context.getColor(R.color.text_secondary));
        chart.getAxisLeft().setTextColor(context.getColor(R.color.text_secondary));
        chart.getXAxis().setTextColor(context.getColor(R.color.text_secondary));
        chart.getAxisRight().setTextColor(context.getColor(R.color.text_secondary));
    }

    private void configurePieChart(PieChart chart) {
        chart.setUsePercentValues(false);
        chart.setDrawEntryLabels(true);
        chart.setHoleRadius(55f);
        chart.setTransparentCircleRadius(60f);
        chart.getDescription().setEnabled(false);
        chart.setEntryLabelColor(context.getColor(R.color.text_primary));
        chart.getLegend().setTextColor(context.getColor(R.color.text_secondary));
    }

    private void configureHorizontalBarChart(HorizontalBarChart chart) {
        chart.setDrawGridBackground(false);
        chart.setFitBars(true);
        chart.setTouchEnabled(true);
        chart.setPinchZoom(true);
        chart.setScaleEnabled(true);
        chart.setExtraLeftOffset(8f);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setTextColor(context.getColor(R.color.text_secondary));
        chart.getAxisLeft().setTextColor(context.getColor(R.color.text_secondary));
        chart.getAxisRight().setTextColor(context.getColor(R.color.text_secondary));
        chart.getXAxis().setTextColor(context.getColor(R.color.text_secondary));
    }

    private PeriodFilter resolvePeriodFilter(List<Integer> checkedIds) {
        if (checkedIds == null || checkedIds.isEmpty()) {
            return periodFilter;
        }
        int checkedId = checkedIds.get(0);
        if (checkedId == R.id.chip_today) {
            return PeriodFilter.TODAY;
        }
        if (checkedId == R.id.chip_yesterday) {
            return PeriodFilter.YESTERDAY;
        }
        if (checkedId == R.id.chip_30d) {
            return PeriodFilter.LAST_30_DAYS;
        }
        if (checkedId == R.id.chip_all) {
            return PeriodFilter.ALL_TIME;
        }
        if (checkedId == R.id.chip_custom) {
            return PeriodFilter.CUSTOM;
        }
        return PeriodFilter.LAST_7_DAYS;
    }

    private BreakdownFilter resolveBreakdownFilter(List<Integer> checkedIds) {
        if (checkedIds == null || checkedIds.isEmpty()) {
            return breakdownFilter;
        }
        int checkedId = checkedIds.get(0);
        if (checkedId == R.id.chip_invoice_status) {
            return BreakdownFilter.INVOICE_STATUS;
        }
        return BreakdownFilter.PAYMENT_METHOD;
    }

    private List<Invoice> filterInvoicesByPeriod(List<Invoice> source, PeriodFilter period) {
        if (source == null || source.isEmpty()) {
            return new ArrayList<>();
        }
        long now = System.currentTimeMillis();
        long start = getPeriodStart(period, now);
        long end = getPeriodEnd(period, now);
        List<Invoice> out = new ArrayList<>();
        for (Invoice invoice : source) {
            if (invoice == null) {
                continue;
            }
            if (period == PeriodFilter.ALL_TIME || invoice.createdAt >= start && invoice.createdAt <= end) {
                out.add(invoice);
            }
        }
        return out;
    }

    private List<Invoice> filterComparisonInvoices(List<Invoice> source, PeriodFilter period) {
        if (period == PeriodFilter.ALL_TIME || period == PeriodFilter.CUSTOM || source == null || source.isEmpty()) {
            return new ArrayList<>();
        }
        long now = System.currentTimeMillis();
        long currentStart = getPeriodStart(period, now);
        long currentEnd = getPeriodEnd(period, now);
        long periodLength = Math.max(1L, currentEnd - currentStart + 1L);
        long previousEnd = Math.max(0L, currentStart - 1L);
        long previousStart = Math.max(0L, previousEnd - periodLength + 1L);

        List<Invoice> out = new ArrayList<>();
        for (Invoice invoice : source) {
            if (invoice == null) {
                continue;
            }
            if (invoice.createdAt >= previousStart && invoice.createdAt <= previousEnd) {
                out.add(invoice);
            }
        }
        return out;
    }

    private long getPeriodStart(PeriodFilter period, long now) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.setTimeInMillis(now);
        switch (period) {
            case TODAY:
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                return calendar.getTimeInMillis();
            case YESTERDAY:
                calendar.add(Calendar.DAY_OF_YEAR, -1);
                return startOfDay(calendar.getTimeInMillis());
            case LAST_7_DAYS:
                calendar.add(Calendar.DAY_OF_YEAR, -6);
                return startOfDay(calendar.getTimeInMillis());
            case LAST_30_DAYS:
                calendar.add(Calendar.DAY_OF_YEAR, -29);
                return startOfDay(calendar.getTimeInMillis());
            case CUSTOM:
                return customStartDate;
            case ALL_TIME:
            default:
                return 0L;
        }
    }

    private long getPeriodEnd(PeriodFilter period, long now) {
        switch (period) {
            case YESTERDAY:
                return startOfDay(now) - 1L;
            case CUSTOM:
                return customEndDate;
            case ALL_TIME:
            case TODAY:
            case LAST_7_DAYS:
            case LAST_30_DAYS:
            default:
                return now;
        }
    }

    private long startOfDay(long timeInMillis) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.setTimeInMillis(timeInMillis);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private List<BuckeTable> buildBuckets(List<Invoice> selectedInvoices, PeriodFilter period) {
        Map<String, BuckeTable> bucketMap = new LinkedHashMap<>();
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        long now = System.currentTimeMillis();
        long start = getPeriodStart(period, now);

        if (period == PeriodFilter.ALL_TIME) {
            if (selectedInvoices.isEmpty()) {
                return new ArrayList<>();
            }
            for (Invoice invoice : selectedInvoices) {
                String label = monthLabel(invoice.createdAt);
                BuckeTable bucket = bucketMap.get(label);
                if (bucket == null) {
                    bucket = new BuckeTable(label, monthStart(invoice.createdAt));
                    bucketMap.put(label, bucket);
                }
                bucket.revenue += invoice.totalAmount;
            }
        } else if (period == PeriodFilter.YESTERDAY) {
            String label = "Yesterday";
            BuckeTable bucket = new BuckeTable(label, start);
            bucketMap.put(label, bucket);
            for (Invoice invoice : selectedInvoices) {
                bucket.revenue += invoice.totalAmount;
            }
        } else if (period == PeriodFilter.CUSTOM) {
            // For custom date range, create day-by-day buckets
            calendar.setTimeInMillis(customStartDate);
            long end = startOfDay(customEndDate);
            while (calendar.getTimeInMillis() <= end) {
                String label = dayLabel(calendar.getTimeInMillis());
                bucketMap.put(label, new BuckeTable(label, calendar.getTimeInMillis()));
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            for (Invoice invoice : selectedInvoices) {
                String label = dayLabel(startOfDay(invoice.createdAt));
                BuckeTable bucket = bucketMap.get(label);
                if (bucket != null) {
                    bucket.revenue += invoice.totalAmount;
                }
            }
        } else {
            calendar.setTimeInMillis(start);
            long end = startOfDay(now);
            while (calendar.getTimeInMillis() <= end) {
                String label = period == PeriodFilter.TODAY
                        ? "Today"
                        : dayLabel(calendar.getTimeInMillis());
                bucketMap.put(label, new BuckeTable(label, calendar.getTimeInMillis()));
                calendar.add(Calendar.DAY_OF_YEAR, 1);
            }
            for (Invoice invoice : selectedInvoices) {
                String label = period == PeriodFilter.TODAY
                        ? "Today"
                        : dayLabel(startOfDay(invoice.createdAt));
                BuckeTable bucket = bucketMap.get(label);
                if (bucket != null) {
                    bucket.revenue += invoice.totalAmount;
                }
            }
        }

        List<BuckeTable> buckets = new ArrayList<>(bucketMap.values());
        Collections.sort(buckets, (left, right) -> Long.compare(left.sortKey, right.sortKey));
        return buckets;
    }

    private String dayLabel(long time) {
        return dayLabelFormat.format(new Date(time));
    }

    private String monthLabel(long time) {
        return monthLabelFormat.format(new Date(time));
    }

    private long monthStart(long time) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private double sumRevenue(List<Invoice> list) {
        double total = 0.0;
        for (Invoice invoice : list) {
            if (invoice != null) {
                total += invoice.totalAmount;
            }
        }
        return total;
    }

    private int countDistinctCustomers(List<Invoice> list) {
        java.util.Set<String> unique = new java.util.HashSet<>();
        for (Invoice invoice : list) {
            if (invoice == null) {
                continue;
            }
            String name = safeValue(invoice.customerName);
            if (!name.isEmpty()) {
                unique.add(name.toLowerCase(Locale.US));
            }
        }
        return unique.size();
    }

    private Map.Entry<String, Double> findTopDoubleEntry(Map<String, Double> map) {
        Map.Entry<String, Double> top = null;
        for (Map.Entry<String, Double> entry : map.entrySet()) {
            if (top == null || entry.getValue() > top.getValue()) {
                top = entry;
            }
        }
        return top;
    }

    private Map.Entry<String, Integer> findTopIntEntry(Map<String, Integer> map) {
        Map.Entry<String, Integer> top = null;
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            if (top == null || entry.getValue() > top.getValue()) {
                top = entry;
            }
        }
        return top;
    }

    private String safeValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "Unknown";
        }
        return value.trim();
    }

    private String normalizePaymentMethod(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "Cash";
        }
        String normalized = value.trim().toLowerCase(Locale.US);
        if (normalized.contains("cash")) return "Cash";
        if (normalized.contains("cheque")) return "Cheque";
        if (normalized.contains("credit")) return "Credit";
        if (normalized.contains("online")) return "Online";
        if (normalized.contains("card")) return "Card";
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1);
    }

    private String normalizeStatus(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "Pending";
        }
        String normalized = value.trim();
        return Character.toUpperCase(normalized.charAt(0)) + normalized.substring(1).toLowerCase(Locale.US);
    }

    private static final class BuckeTable {
        final String label;
        final long sortKey;
        double revenue;

        BuckeTable(String label, long sortKey) {
            this.label = label;
            this.sortKey = sortKey;
            this.revenue = 0.0;
        }
    }

    private static final class ProductStats {
        final String name;
        double revenue;
        int units;

        ProductStats(String name) {
            this.name = name;
        }
    }
}
