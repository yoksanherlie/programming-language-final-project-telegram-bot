package com.binus;

import com.binus.web.Order;
import com.binus.web.OrderRepository;
import org.telegram.telegrambots.api.methods.AnswerPreCheckoutQuery;
import org.telegram.telegrambots.api.methods.AnswerShippingQuery;
import org.telegram.telegrambots.api.methods.send.SendInvoice;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.MessageEntity;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.api.objects.payments.ShippingOption;
import org.telegram.telegrambots.api.objects.payments.SuccessfulPayment;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

public class PaymentBot extends TelegramLongPollingBot {

    public static String PAY_COMMAND = "/pay";
    public static String BOT_PAYMENT_TOKEN = "361519591:TEST:2ad78de17e177f789d481844ab2e8b84";

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            String messageText = message.getText();
            if (message.isCommand()) {
                MessageEntity entity = message.getEntities().get(0);
                String command = messageText.substring(0, entity.getLength());

                if (command.equals(PAY_COMMAND)) {
                    this.doPayment(update);
                }
            }
        }

        if (update.hasShippingQuery()) {
            this.doShippingMethod(update);
        }

        if (update.hasPreCheckoutQuery()) {
            this.doCheckout(update);
        }

        if (update.getMessage().hasSuccessfulPayment()) {
            this.updateOrderData(update);
        }
    }

    @Override
    public String getBotUsername() {
        return "pl_fp_bot";
    }

    @Override
    public String getBotToken() {
        return "599131990:AAFppQElJljID3iEFx98iLKy2SJn3C2ItTg";
    }

    public void doPayment(Update update) {
        String arrayText[] = update.getMessage().getText().split(" ");

        if (arrayText.length == 1) {
            SendMessage snd = new SendMessage();
            snd.setChatId(update.getMessage().getChatId());
            snd.setText("Please provide the invoice number");

            try {
                execute(snd);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        } else {
            String invoiceNo = arrayText[1];

            OrderRepository orderRepo = new OrderRepository();
            if (orderRepo.checkExistOrder(invoiceNo) == 1) {
                Order order = orderRepo.getOrderById(invoiceNo);

                if (order.getStatus().equals("done")) {
                    SendMessage snd = new SendMessage();
                    snd.setChatId(update.getMessage().getChatId());
                    snd.setText("This order has already been paid");

                    try {
                        execute(snd);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }

                } else {
                    SendInvoice senderInvoice = new SendInvoice();
                    List<LabeledPrice> prices = new ArrayList<LabeledPrice>();
                    prices.add(new LabeledPrice("Total", (int) order.getTotalPrice()));

                    senderInvoice.setChatId(Integer.parseInt(update.getMessage().getChatId().toString()));
                    senderInvoice.setTitle("Programming Language Final Project Payment");
                    senderInvoice.setDescription("This is the payment for invoice: " + invoiceNo);
                    senderInvoice.setPayload(invoiceNo);
                    senderInvoice.setProviderToken(BOT_PAYMENT_TOKEN);
                    senderInvoice.setStartParameter("hehe");
                    senderInvoice.setCurrency("USD");
                    senderInvoice.setPrices(prices);
                    senderInvoice.setNeedName(true);
                    senderInvoice.setNeedEmail(true);
                    senderInvoice.setNeedShippingAddress(true);
                    senderInvoice.setFlexible(true);

                    try {
                        execute(senderInvoice);
                    } catch (TelegramApiException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                SendMessage snd = new SendMessage();
                snd.setChatId(update.getMessage().getChatId());
                snd.setText("Invoice number does not exist");

                try {
                    execute(snd);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void doShippingMethod(Update update) {
        List<LabeledPrice> prices = new ArrayList<LabeledPrice>();
        prices.add(new LabeledPrice("shipping-price", 500));

        List<ShippingOption> options = new ArrayList<ShippingOption>();
        options.add(new ShippingOption("jne", "JNE", prices));
        options.add(new ShippingOption("go-send", "GO-Send", prices));

        AnswerShippingQuery shippingQuery = new AnswerShippingQuery();
        shippingQuery.setShippingQueryId(update.getShippingQuery().getId());
        shippingQuery.setOk(true);
        shippingQuery.setShippingOptions(options);

        try {
            execute(shippingQuery);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void doCheckout(Update update) {
        AnswerPreCheckoutQuery checkoutQuery = new AnswerPreCheckoutQuery(update.getPreCheckoutQuery().getId(), true);

        try {
            execute(checkoutQuery);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void updateOrderData(Update update) {
        SuccessfulPayment payment = update.getMessage().getSuccessfulPayment();

        int orderId = Integer.parseInt(payment.getInvoicePayload());

        OrderRepository orderRepo = new OrderRepository();
        orderRepo.updateFinalOrderById(orderId, payment.getTotalAmount(), payment.getShippingOptionId());
    }
}
