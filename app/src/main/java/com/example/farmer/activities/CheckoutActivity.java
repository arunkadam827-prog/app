package com.example.farmer.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.farmer.R;
import com.example.farmer.models.Order;
import com.example.farmer.services.ApiService;
import com.example.farmer.services.RetrofitClient;
import com.example.farmer.utils.SessionManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutActivity extends AppCompatActivity {

    public static final String EXTRA_TOTAL_AMOUNT = "extra_total_amount";
    public static final String EXTRA_ITEMS_COUNT = "extra_items_count";

    private EditText etCustomerName, etCustomerPhone, etDeliveryAddress, etDeliveryCity;
    private TextView tvSummarySubtotal, tvSummaryTotal, tvSummaryItemsLabel;
    private RadioGroup rgPaymentMethods;
    private LinearLayout layoutUpi, layoutCard, layoutCompanyAcc;
    private TextView layoutCod;

    private EditText etBuyerUpiId, etCardNumber, etCardHolder, etCardExpiry, etCardCvv, etUtrRef;
    private Button btnCopyUpi, btnCopyCompAcc, btnPayPlaceOrder;
    private ProgressBar progressCheckout;

    private SessionManager sessionManager;
    private ApiService apiService;
    private double totalAmount = 0.0;
    private int itemsCount = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        sessionManager = new SessionManager(this);
        apiService = RetrofitClient.getApiService();

        totalAmount = getIntent().getDoubleExtra(EXTRA_TOTAL_AMOUNT, 0.0);
        itemsCount = getIntent().getIntExtra(EXTRA_ITEMS_COUNT, 1);

        initViews();
        setupData();
        setupListeners();
    }

    private void initViews() {
        Toolbar toolbar = findViewById(R.id.toolbar_checkout);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }

        etCustomerName = findViewById(R.id.et_customer_name);
        etCustomerPhone = findViewById(R.id.et_customer_phone);
        etDeliveryAddress = findViewById(R.id.et_delivery_address);
        etDeliveryCity = findViewById(R.id.et_delivery_city);

        tvSummarySubtotal = findViewById(R.id.tv_summary_subtotal);
        tvSummaryTotal = findViewById(R.id.tv_summary_total);
        tvSummaryItemsLabel = findViewById(R.id.tv_summary_items_label);

        rgPaymentMethods = findViewById(R.id.rg_payment_methods);
        layoutUpi = findViewById(R.id.layout_upi_details);
        layoutCard = findViewById(R.id.layout_card_details);
        layoutCompanyAcc = findViewById(R.id.layout_company_acc_details);
        layoutCod = findViewById(R.id.layout_cod_details);

        etBuyerUpiId = findViewById(R.id.et_buyer_upi_id);
        etCardNumber = findViewById(R.id.et_card_number);
        etCardHolder = findViewById(R.id.et_card_holder);
        etCardExpiry = findViewById(R.id.et_card_expiry);
        etCardCvv = findViewById(R.id.et_card_cvv);
        etUtrRef = findViewById(R.id.et_utr_ref);

        btnCopyUpi = findViewById(R.id.btn_copy_upi);
        btnCopyCompAcc = findViewById(R.id.btn_copy_company_acc);
        btnPayPlaceOrder = findViewById(R.id.btn_pay_place_order);
        progressCheckout = findViewById(R.id.progress_checkout);
    }

    private void setupData() {
        etCustomerName.setText(sessionManager.getUserName());
        etCustomerPhone.setText(sessionManager.getUserPhone());
        etDeliveryAddress.setText(sessionManager.getUserAddress());
        etDeliveryCity.setText(sessionManager.getUserCity());

        tvSummaryItemsLabel.setText(String.format("Items Subtotal (%d item%s)", itemsCount, itemsCount > 1 ? "s" : ""));
        tvSummarySubtotal.setText(String.format("₹ %.2f", totalAmount));
        tvSummaryTotal.setText(String.format("₹ %.2f", totalAmount));
        btnPayPlaceOrder.setText(String.format("Pay ₹ %.2f & Place Order", totalAmount));
    }

    private void setupListeners() {
        rgPaymentMethods.setOnCheckedChangeListener((group, checkedId) -> {
            layoutUpi.setVisibility(checkedId == R.id.rb_upi ? View.VISIBLE : View.GONE);
            layoutCard.setVisibility(checkedId == R.id.rb_card ? View.VISIBLE : View.GONE);
            layoutCompanyAcc.setVisibility(checkedId == R.id.rb_company_acc ? View.VISIBLE : View.GONE);
            layoutCod.setVisibility(checkedId == R.id.rb_cod ? View.VISIBLE : View.GONE);
        });

        btnCopyUpi.setOnClickListener(v -> {
            copyToClipboard("Company UPI", "9356601104@upi");
            Toast.makeText(this, "Company UPI / Number Copied: 9356601104@upi", Toast.LENGTH_SHORT).show();
        });

        btnCopyCompAcc.setOnClickListener(v -> {
            String details = "Company: Kisan Connect Agro Services Pvt. Ltd.\n" +
                    "A/C No: 9356601104\n" +
                    "UPI / Mobile: 9356601104@upi\n" +
                    "IFSC: HDFC0001234\n" +
                    "Bank: HDFC Bank, Commercial Agri Branch";
            copyToClipboard("Company Bank Details", details);
            Toast.makeText(this, "Company Bank Details copied: 9356601104", Toast.LENGTH_SHORT).show();
        });

        btnPayPlaceOrder.setOnClickListener(v -> attemptPlaceOrder());
    }

    private void attemptPlaceOrder() {
        String name = etCustomerName.getText().toString().trim();
        String phone = etCustomerPhone.getText().toString().trim();
        String address = etDeliveryAddress.getText().toString().trim();
        String city = etDeliveryCity.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etCustomerName.setError("Name required");
            etCustomerName.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            etCustomerPhone.setError("Phone number required");
            etCustomerPhone.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(address)) {
            etDeliveryAddress.setError("Delivery address required");
            etDeliveryAddress.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(city)) {
            etDeliveryCity.setError("City required");
            etDeliveryCity.requestFocus();
            return;
        }

        int selectedPaymentId = rgPaymentMethods.getCheckedRadioButtonId();
        String paymentMethod;

        if (selectedPaymentId == R.id.rb_upi) {
            String upiId = etBuyerUpiId.getText().toString().trim();
            paymentMethod = !upiId.isEmpty() ? "UPI (" + upiId + ")" : "UPI (Google Pay / PhonePe)";
        } else if (selectedPaymentId == R.id.rb_card) {
            String cardNo = etCardNumber.getText().toString().trim();
            if (cardNo.length() < 12) {
                etCardNumber.setError("Enter a valid 16-digit card number");
                etCardNumber.requestFocus();
                return;
            }
            String last4 = cardNo.length() >= 4 ? cardNo.substring(cardNo.length() - 4) : cardNo;
            paymentMethod = "Card (ending in " + last4 + ")";
        } else if (selectedPaymentId == R.id.rb_company_acc) {
            String utr = etUtrRef.getText().toString().trim();
            paymentMethod = "Company Bank Transfer" + (!utr.isEmpty() ? " (Ref: " + utr + ")" : "");
        } else {
            paymentMethod = "Cash on Delivery";
        }

        String fullDeliveryAddress = name + ", " + address + ", " + city + " (Ph: " + phone + ")";

        // Dispatch order creation to backend
        setLoading(true);
        long userId = sessionManager.getUserId();

        Map<String, Object> orderRequest = new HashMap<>();
        orderRequest.put("userId", userId);
        orderRequest.put("deliveryAddress", fullDeliveryAddress);
        orderRequest.put("paymentMethod", paymentMethod);

        apiService.createOrder(orderRequest).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(@NonNull Call<Order> call, @NonNull Response<Order> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Order placedOrder = response.body();
                    showSuccessDialog(placedOrder.getOrderId(), paymentMethod, fullDeliveryAddress);
                } else {
                    // Do NOT fake a successful order - surface the real failure so the
                    // user knows the order was not placed.
                    Log.e("CheckoutActivity", "Order failed: HTTP " + response.code() + " " + response.message());
                    Toast.makeText(CheckoutActivity.this,
                            "Could not place order. Please try again.",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Order> call, @NonNull Throwable t) {
                setLoading(false);
                Log.e("CheckoutActivity", "Network error: " + t.getMessage());
                Toast.makeText(CheckoutActivity.this,
                        "Network error. Order not placed. Check your connection.",
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showSuccessDialog(int orderId, String paymentMethod, String address) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_order_success, null);
        builder.setView(dialogView);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView tvOrderId = dialogView.findViewById(R.id.tv_dialog_order_id);
        TextView tvPayment = dialogView.findViewById(R.id.tv_dialog_payment_info);
        TextView tvDelivery = dialogView.findViewById(R.id.tv_dialog_delivery_info);
        Button btnViewOrders = dialogView.findViewById(R.id.btn_dialog_view_orders);
        Button btnContinueShopping = dialogView.findViewById(R.id.btn_dialog_continue_shopping);

        tvOrderId.setText("Order #" + orderId);
        tvPayment.setText("Paid via: " + paymentMethod);
        tvDelivery.setText("Delivering to: " + address);

        btnViewOrders.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(this, OrdersActivity.class);
            startActivity(intent);
            finish();
        });

        btnContinueShopping.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialog.show();
    }

    private void copyToClipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
        }
    }

    private void setLoading(boolean isLoading) {
        progressCheckout.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnPayPlaceOrder.setEnabled(!isLoading);
    }
}
