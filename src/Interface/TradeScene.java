package Interface;

import ClientServer.MessageServer;
import ClientServer.MessageType;
import ClientServer.ServerIP;
import TradeCenter.Card.Card;
import TradeCenter.Customers.Collection;
import TradeCenter.Customers.Customer;
import TradeCenter.Exceptions.TradeExceptions.AlreadyStartedTradeException;
import TradeCenter.Trades.ATrade;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import static Interface.SearchUserScene.retrieveCustomer;


public class TradeScene {

    private static HBox buttonsBox;
    private static GridPane mainGrid;
    private static ScrollPane myCollectionGrid;
    private static ScrollPane otherCollectionGrid;
    private static ScrollPane myOfferGrid;
    private static ScrollPane otherOfferGrid;
    private static BorderPane myCollectionPane;
    private static BorderPane otherCollectionPane;
    private static BorderPane myOfferPane;
    private static BorderPane otherOfferPane;
    private static BorderPane mainPane;
    private static Customer myC;
    private static Customer otherC;
    private static ArrayList<Card> myImageList = new ArrayList<Card>();
    private static ArrayList<Card> otherImageList = new ArrayList<Card>();
    private static FlowPane myCollFlow;
    private static FlowPane otherCollFlow;
    private static FlowPane myOfferFlow;
    private static FlowPane otherOfferFlow;
    private static ArrayList<Card> myCollectionList = new ArrayList<Card>();
    private static ArrayList<Card> otherCollectionList = new ArrayList<Card>();
    private static Text myCollection;
    private static Text otherCollection;
    private static Text myOffer;
    private static Text otherOffer;
    private static TextFlow myCollectionTitle;
    private static TextFlow otherCollectionTitle;
    private static TextFlow myOfferTitle;
    private static TextFlow otherOfferTitle;

    private static Collection myCardOffer;
    private static Collection otherCardOffer;


    private static ATrade currentTrade = null;

    static BorderPane display(ATrade trade, Customer myCustomer, Customer otherCustomer,  boolean flagStarted, boolean changedMind){

        currentTrade=trade;

        myCardOffer = new Collection();
        otherCardOffer = new Collection();
        myC = myCustomer;
        otherC = otherCustomer;
        myImageList.removeAll(myImageList);
        otherImageList.removeAll(otherImageList);
        myCollectionList.removeAll(myCollectionList);
        otherCollectionList.removeAll(otherCollectionList);
        mainPane = new BorderPane();
        myCollFlow = new FlowPane();
        otherCollFlow = new FlowPane();
        myOfferFlow = new FlowPane();
        otherOfferFlow = new FlowPane();
        //griglia
        mainGrid = new GridPane();
        mainGrid.setHgap(10);
        mainGrid.setVgap(10);
        myCollection = new Text(myCustomer.getUsername());
        myCollectionTitle = new TextFlow(myCollection);
        myCollectionTitle.setPadding(new Insets(5));
        myCollectionTitle.setStyle("-fx-background-color: #aa12ff");
        otherCollection = new Text(otherCustomer.getUsername()+ "'s Collection");
        otherCollectionTitle = new TextFlow(otherCollection);
        otherCollectionTitle.setPadding(new Insets(5));
        otherCollectionTitle.setStyle("-fx-background-color: #aa12ff");
        myOffer = new Text("myOffer");
        myOfferTitle = new TextFlow(myOffer);
        myOfferTitle.setPadding(new Insets(5));
        myOfferTitle.setStyle("-fx-background-color: #aa12ff");
        otherOffer = new Text(otherCustomer.getUsername()+ "'s Offer");
        otherOfferTitle = new TextFlow(otherOffer);
        otherOfferTitle.setPadding(new Insets(5));
        otherOfferTitle.setStyle("-fx-background-color: #aa12ff");

        //griglie

        myCollectionGrid = new ScrollPane();
        otherCollectionGrid = new ScrollPane();
        myOfferGrid = new ScrollPane();
        otherOfferGrid = new ScrollPane();

        myCollectionGrid.setMinSize(405,240);
        myCollectionGrid.setMaxSize(405,240);
        otherCollectionGrid.setMinSize(405,240);
        otherCollectionGrid.setMaxSize(405,240);
        myOfferGrid.setMinSize(405,233);
        myOfferGrid.setMaxSize(405,233);
        otherOfferGrid.setMinSize(405,233);
        otherOfferGrid.setMaxSize(405,233);
        myCollectionPane = new BorderPane();
        otherCollectionPane = new BorderPane();
        myOfferPane = new BorderPane();
        otherOfferPane = new BorderPane();

        if(!flagStarted) {
            //costruisco le singole griglie
            myCollectionPane = displayCards(myCustomer, myCollectionTitle, myCollectionGrid, myCollFlow, true, myCollectionList);
            otherCollectionPane = displayCards(otherCustomer, otherCollectionTitle, otherCollectionGrid, otherCollFlow, false, otherCollectionList);
            myOfferPane = displayCards(null, myOfferTitle, myOfferGrid, myOfferFlow, false, null);
            otherOfferPane = displayCards(null, otherOfferTitle, otherOfferGrid, otherOfferFlow, false, null);

            //myCollectionPane.setMaxHeight(50);
            //myCollectionPane.setMaxWidth(60);
            //costruisco la griglia principale, aggiungendoci le singole
            mainGrid.add(myCollectionPane, 1, 1);
            mainGrid.add(otherCollectionPane, 1, 2);
            mainGrid.add(myOfferPane, 2, 1);
            mainGrid.add(otherOfferPane, 2, 2);
            mainGrid.setStyle("-fx-background-color: #55ff44");
            mainGrid.setAlignment(Pos.CENTER);
        }
        //bottoni
        buttonsBox = new HBox();
        buttonsBox.setPadding(new Insets(7, 20, 7, 20));
        buttonsBox.setSpacing(10);
        buttonsBox.setStyle("-fx-background-color: #aa12ff");
        Button refuse = new Button("Refuse");
        Button raise = new Button("Raise");
        Button accept = new Button("Accept");

        //listener bottoni
        raise.setOnAction(event -> {
            try {
                Socket socket = new Socket(ServerIP.ip, ServerIP.port);
                ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
                if(!flagStarted){

                    if(!myCardOffer.collectionIsEmpty() && !otherCardOffer.collectionIsEmpty()) {

                            MainWindow.addDynamicContent(InfoScene.display("Your offer has been sent", "Interface/imagePack/infoSign.png", false));
                            os.writeObject(new MessageServer(MessageType.CREATEOFFER, myC.getId(), otherC.getId(), myCardOffer, otherCardOffer));
                            Thread.sleep(100);

                    } else {
                        MainWindow.addDynamicContent(InfoScene.display("You can't offer empty\ncollections", "Interface/imagePack/2000px-Simple_Alert.svg.png", true));
                    }
                }else {
                    if (verifyUpdated(currentTrade)) {
                        Customer currentMy = null;
                        Customer currentOther = null;
                        updateCustomers();
                        if (myC.getId().equals(currentTrade.getCustomer1())) {
                            currentMy = myC;
                            currentOther = otherC;
                        } else {
                            currentMy = otherC;
                            currentOther = myC;
                        }
                        if(!myCardOffer.collectionIsEmpty() && !otherCardOffer.collectionIsEmpty()) {
                            if (stillInTheCollection(currentMy.getCollection(), currentTrade.getOffer1()) && stillInTheCollection(currentOther.getCollection(), currentTrade.getOffer2())) {
                                if (myC.getId().equals(currentTrade.getCustomer1())) {
                                    os.writeObject(new MessageServer(MessageType.RAISEOFFER, myC.getId(), otherC.getId(), myCardOffer, otherCardOffer, changedMind));
                                } else {
                                    os.writeObject(new MessageServer(MessageType.RAISEOFFER, currentMy.getId(), currentOther.getId(), otherCardOffer, myCardOffer, false));
                                }
                                ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
                                Object read = is.readObject();
                                Thread.sleep(100);
                                boolean flag = read instanceof AlreadyStartedTradeException;
                                if (!flag) {
                                    myCardOffer.getSet().remove(myCardOffer.getSet());
                                    otherCardOffer.getSet().remove(otherCardOffer.getSet());
                                    MainWindow.addDynamicContent(InfoScene.display("Offer changed", "Interface/imagePack/infoSign.png", false));
                                    System.out.println("raised new offer");
                                } else {
                                    throw new AlreadyStartedTradeException(otherC.getUsername());
                                }
                            } else {
                                removeTrade(myC.getId(), otherC.getId());
                                MainWindow.refreshDynamicContent(TradeScene.display(null, myC, otherC, false, false));
                                MainWindow.addDynamicContent(InfoScene.display("The other customer traded one or\n more cards with someone else\nthe trade is restarted", "Interface/imagePack/2000px-Simple_Alert.svg.png", true));
                            }
                        }else {
                            MainWindow.addDynamicContent(InfoScene.display("You can't offer empty\ncollections", "Interface/imagePack/2000px-Simple_Alert.svg.png", true));
                        }

                    }else{
                        if(currentTrade!=null) {
                            infoOfferChanged();
                        }else{
                            MainWindow.addDynamicContent(InfoScene.display("The trade has already been closed\nby the other customer\nsee the result in My Trade", "Interface/imagePack/2000px-Simple_Alert.svg.png", false));
                        }
                    }
                }

                    socket.close();

            } catch (IOException | ClassNotFoundException | AlreadyStartedTradeException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        refuse.setOnAction(event -> {
            if(verifyUpdated(currentTrade)) {
                    try {
                        MainWindow.addDynamicContent(InfoScene.display("Offer rejected", "Interface/imagePack/infoSign.png", false));
                        Socket socket = new Socket(ServerIP.ip, ServerIP.port);
                        ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
                        os.writeObject(new MessageServer(MessageType.ENDTRADE, trade, false));
                        Thread.sleep(100);
                        socket.close();
                        System.out.println("refused offer");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

            } else{
                if(currentTrade!=null) {
                    infoOfferChanged();
                }else{
                    MainWindow.addDynamicContent(InfoScene.display("The trade has already been closed\nby the other customer\nsee the result in My Trade", "Interface/imagePack/2000px-Simple_Alert.svg.png", false));
                }
            }
        });


        accept.setOnAction(event -> {
            Customer currentmy = null;
            Customer currentOther= null;
            updateCustomers();
            boolean condition = myC.getId().equals(currentTrade.getCustomer2());


            if(condition){
                if(verifyUpdated(currentTrade)) {
                    if(myC.getId().equals(currentTrade.getCustomer1())){
                        currentmy = myC;
                        currentOther = otherC;
                    }
                    else {
                        currentmy = otherC;
                        currentOther = myC;
                    }
                    updateCustomers();
                        if(stillInTheCollection(currentmy.getCollection(),currentTrade.getOffer1()) && stillInTheCollection(currentOther.getCollection(),currentTrade.getOffer2())) {
                            Timeline task = loading(currentTrade);
                            task.playFromStart();
                       }else{
                            removeTrade(myC.getId(),otherC.getId());
                            MainWindow.refreshDynamicContent(TradeScene.display(null, myC, otherC,false, false));
                            MainWindow.addDynamicContent(InfoScene.display("The other customer traded one or\n more cards with someone else\nthe trade is restarted", "Interface/imagePack/2000px-Simple_Alert.svg.png", true));
                        }

                }
                else {
                    if(currentTrade!=null) {
                        infoOfferChanged();
                    }else {
                        MainWindow.addDynamicContent(InfoScene.display("The trade has already been closed\nby the other customer\nsee the result in My Trade", "Interface/imagePack/2000px-Simple_Alert.svg.png", false));
                    }
                }
            }else{
                MainWindow.addDynamicContent(InfoScene.display("You can't accept your own offer", "Interface/imagePack/2000px-Simple_Alert.svg.png", true));
                System.err.println("You cannot accept your own offer Cugghiuna!");
            }
        });
        //buttonsBox.getChildren().addAll(refuse, raise, accept);
        if(!flagStarted) {
            raise.setText("New Trade");
            buttonsBox.getChildren().addAll(raise);
            mainPane.setTop(mainGrid);
            mainPane.setBottom(buttonsBox);
        }else{
            buttonsBox.getChildren().addAll(refuse, raise, accept);
            if(trade!=null) {
                restoreFromPreviousTrade(trade);
            }
        }
        mainPane.getStylesheets().add("Interface/ButtonsCSS.css");
        return mainPane;
    }

    static BorderPane displayCards(Customer customer, TextFlow title, ScrollPane grid, FlowPane flowPane, boolean flag, ArrayList<Card> collection){
        BorderPane pane = new BorderPane();
        pane.setTop(title);                 //titolo
        //FlowPane flowPane = new FlowPane();
        flowPane.setStyle("-fx-background-color: #fff910");


        if(customer == null){
            grid.setFitToWidth(true);
            grid.setFitToHeight(true);
            grid.setContent(flowPane);
            grid.setStyle("-fx-background-color: #fffb48");

            pane.setCenter(grid);
            return pane;
        }

        //ciclo per aggiungere carte
        for (Card card : customer.getCollection()){
            if(collection != null){
                collection.add(card);
            }
            BorderPane cardPane = new BorderPane();
            Image image = SwingFXUtils.toFXImage(card.getDescription().getPic(), null);
            ImageView imageView = new ImageView();
            imageView.setImage(image);
            imageView.setPreserveRatio(true);
            imageView.setFitHeight(161);
            cardPane.setCenter(imageView);

            Tooltip tooltip = new Tooltip();

            Tooltip.install(imageView, new Tooltip("Right Click To Zoom"));

            imageView.setOnMousePressed(moveCardsCollection(imageView,flag,card));

            flowPane.getChildren().add(cardPane);
            flowPane.setMargin(cardPane, new Insets(10, 5, 10, 5));
        }

        grid.setFitToWidth(true);
        grid.setFitToHeight(true);
        grid.setContent(flowPane);
        grid.setStyle("-fx-background-color: #fffb48");

        pane.setCenter(grid);
        return pane;
    }

    static ScrollPane addToOffer(ScrollPane scrollPane, Card card, boolean flag){
        ArrayList<Card> imageList;

        if(flag){
            myCardOffer.addCardToCollection(card);
        }
        else{
            otherCardOffer.addCardToCollection(card);
        }

        if(flag) {
            myImageList.add(card);
            imageList=myImageList;
        }
        else{
            otherImageList.add(card);
            imageList = otherImageList;
        }
        FlowPane flow = new FlowPane();
        //flow.getChildren().remove(imageView);
        flow.setStyle("-fx-background-color: #fff910");
        for (Card c: imageList) {
            BorderPane pane = new BorderPane();
            Image image = SwingFXUtils.toFXImage(c.getDescription().getPic(), null);
            ImageView imageView1 = new ImageView();
            imageView1.setImage(image);
            imageView1.setPreserveRatio(true);
            imageView1.setFitHeight(161);
            imageView1.setOnMousePressed(moveCardsOffer(flow,pane,flag,c));

            pane.setCenter(imageView1);
            flow.getChildren().add(pane);
            flow.setMargin(pane, new Insets(10, 5, 10, 5));
        }
        scrollPane.setContent(flow);
        return scrollPane;
    }

    static void restoreCollection(Card card, boolean flag, FlowPane flowPane, ArrayList<Card> cardList){
        flowPane.getChildren().removeAll(flowPane.getChildren());
        if(card!=null) {
            cardList.add(card);
        }
        for(Card c : cardList) {
                BorderPane cardPane = new BorderPane();
                Image image = SwingFXUtils.toFXImage(c.getDescription().getPic(), null);
                ImageView imageView = new ImageView();
                imageView.setImage(image);
                imageView.setPreserveRatio(true);
                imageView.setFitHeight(161);
                cardPane.setCenter(imageView);

                Tooltip tooltip = new Tooltip();

                Tooltip.install(imageView, new Tooltip("Right Click To Zoom"));

                imageView.setOnMousePressed(moveCardsCollection(imageView,flag,c));

                flowPane.getChildren().add(cardPane);
                flowPane.setMargin(cardPane, new Insets(10, 5, 10, 5));
            }

    }

    static void restoreFromPreviousTrade(ATrade trade){
        GridPane mainGrid1 = new GridPane();
        myImageList.removeAll(myImageList);
        otherImageList.removeAll(otherImageList);
        myCollectionList.removeAll(myCollectionList);
        otherCollectionList.removeAll(otherCollectionList);


        restoreScroll(myCollectionPane, myCollectionGrid, myCollFlow);
        restoreScroll(myOfferPane, myOfferGrid, myOfferFlow);
        restoreScroll(otherCollectionPane, otherCollectionGrid,otherCollFlow);
        restoreScroll(otherOfferPane, otherOfferGrid,otherOfferFlow);

        for (Card card : myC.getCollection()) {
            myCollectionList.add(card);
        }

        for (Card card : otherC.getCollection()) {
            otherCollectionList.add(card);
        }
        if(myC.getId().equals(trade.getCustomer1())) {
            for (Card card : trade.getOffer1()) {
                addToOffer(myOfferGrid, card, true);
            }

            for (Card card : trade.getOffer2()) {
                addToOffer(otherOfferGrid, card, false);
            }
        }else{
            for (Card card : trade.getOffer2()) {
                addToOffer(myOfferGrid, card, true);
            }

            for (Card card : trade.getOffer1()) {
                addToOffer(otherOfferGrid, card, false);
            }
        }



        myCollectionList.removeAll(myImageList);
        otherCollectionList.removeAll(otherImageList);
        restoreCollection(null, true, myCollFlow, myCollectionList);
        restoreCollection(null, false, otherCollFlow, otherCollectionList);

        myCollectionPane.setCenter(myCollectionGrid);
        myCollectionPane.setTop(myCollectionTitle);
        myOfferPane.setCenter(myOfferGrid);
        myOfferPane.setTop(myOfferTitle);
        otherCollectionPane.setCenter(otherCollectionGrid);
        otherCollectionPane.setTop(otherCollectionTitle);
        otherOfferPane.setCenter(otherOfferGrid);
        otherOfferPane.setTop(otherOfferTitle);

        mainGrid1.setHgap(10);
        mainGrid1.setVgap(10);

        mainGrid1.add(myCollectionPane,1,1);
        mainGrid1.add(otherCollectionPane,1,2);
        mainGrid1.add(myOfferPane,2,1);
        mainGrid1.add(otherOfferPane,2,2);
        mainGrid1.setStyle("-fx-background-color: #55ff44");
        mainGrid1.setAlignment(Pos.CENTER);


        mainPane.setCenter(mainGrid1);
        mainPane.setBottom(buttonsBox);


    }

    static void restoreScroll(BorderPane borderPane,ScrollPane scrollPane, FlowPane flowPane){
        borderPane.setStyle("-fx-background-color: #fff910");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setContent(flowPane);
        scrollPane.setStyle("-fx-background-color: #fffb48");
        flowPane.setStyle("-fx-background-color: #fff910");
        scrollPane.setStyle("-fx-background-color: #fff910");
        scrollPane.setContent(flowPane);

    }

    static BorderPane refresh(){

        GridPane mainGrid1 = new GridPane();

        mainGrid1.setHgap(10);
        mainGrid1.setVgap(10);

        restoreCollection(null, true, myCollFlow, myCollectionList);
        restoreCollection(null, false, otherCollFlow, otherCollectionList);

        mainGrid1.add(myCollectionPane,1,1);
        mainGrid1.add(otherCollectionPane,1,2);
        mainGrid1.add(myOfferPane,2,1);
        mainGrid1.add(otherOfferPane,2,2);
        mainGrid1.setStyle("-fx-background-color: #55ff44");
        mainGrid1.setAlignment(Pos.CENTER);


        mainPane.setCenter(mainGrid1);
        mainPane.setBottom(buttonsBox);

        return mainPane;
    }

    private static Timeline loading(ATrade trade ){

        Timeline task = new Timeline(

                new KeyFrame(
                        Duration.ZERO,
                        event -> {

                            MainWindow.addDynamicContent(InfoScene.display("Deal done", "Interface/imagePack/pokeBall.png",false));
                        }
                ),

                new KeyFrame(
                        Duration.millis(5),
                        event -> {

                            try {
                                Socket socket = new Socket(ServerIP.ip, ServerIP.port);
                                ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
                                os.writeObject(new MessageServer(MessageType.ENDTRADE,  trade, true));
                                try {
                                    //todo fix sleep
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                socket.close();
                                System.out.println("accepted offer");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                )
        );

        return task;
    }

    private static ATrade retrieveActualTrade(ATrade trade){
        ATrade actualTrade = null;
        try {
            Socket socket = new Socket(ServerIP.ip, ServerIP.port);
            socket.setTcpNoDelay(true);
            //socket.setKeepAlive(true);
            ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
            os.writeObject(new MessageServer(MessageType.SEARCHTRADE, trade.getCustomer1(), trade.getCustomer2()));
            ObjectInputStream is = new ObjectInputStream(socket.getInputStream());
            actualTrade = (ATrade) is.readObject();
            os.flush();
            socket.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return actualTrade;
    }

    private static boolean verifyUpdated(ATrade trade){
        ATrade actualTrade = retrieveActualTrade(trade);
        if(actualTrade!=null) {
            if (trade.getOffer1().getSet().size() == actualTrade.getOffer1().getSet().size() && trade.getOffer2().getSet().size() == actualTrade.getOffer2().getSet().size()) {
                if (trade.getOffer1().getSet().equals(actualTrade.getOffer1().getSet()) && trade.getOffer2().getSet().equals(actualTrade.getOffer2().getSet())) {
                    return true;
                }
            }
        }
        currentTrade = actualTrade;
        return false;
    }

    private static void infoOfferChanged(){
        updateCustomers();
        restoreFromPreviousTrade(currentTrade);
        MainWindow.addDynamicContent(InfoScene.display("The other customer changed\nthe offer", "Interface/imagePack/2000px-Simple_Alert.svg.png",true));
    }

    private static boolean stillInTheCollection(Collection collection, Collection offer){
        for(Card card : offer){
            if(!collection.isInTheCollection(card)){
                return false;
            }
        }

        return true;
    }

    private static void removeTrade(String myid, String otherId){
        Socket socket = null;
        try {
            socket = new Socket(ServerIP.ip, ServerIP.port);
            ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
            os.writeObject(new MessageServer(MessageType.REMOVETRADE, myid, otherId));
            Thread.sleep(100);
            socket.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void updateCustomers(){
        Customer currentMy = null;
        Customer currentOther = null;
        currentMy = retrieveCustomer(myC.getUsername());
        currentOther = retrieveCustomer(otherC.getUsername());

        myC=currentMy;
        otherC=currentOther;
    }

    private static EventHandler<MouseEvent> moveCardsCollection(ImageView imageView, boolean flag, Card card){
        EventHandler<MouseEvent> event = new EventHandler<MouseEvent>() {
            public void handle(MouseEvent mouseEvent1) {


                if (mouseEvent1.getButton().equals(MouseButton.SECONDARY)) {
                    if (mouseEvent1.getClickCount() == 1) {
                        MainWindow.refreshDynamicContent(Demo.display(imageView, "trade"));
                    }
                }
                if (flag) {
                    if (mouseEvent1.getButton().equals(MouseButton.PRIMARY)) {
                        if (mouseEvent1.getClickCount() == 1) {

                            handleEventCollection(myCollectionList,card,flag,myOfferGrid,myOfferPane,myCollFlow);

                        }
                    }
                } else {
                    if (mouseEvent1.getButton().equals(MouseButton.PRIMARY)) {
                        if (mouseEvent1.getClickCount() == 1) {

                            handleEventCollection(otherCollectionList,card,flag,otherOfferGrid,otherOfferPane,otherCollFlow);
                        }
                    }
                }
            }

        };

        return event;
    }

    private static void handleEventCollection(ArrayList<Card> collectionList, Card card, boolean flag, ScrollPane offerGrid, BorderPane offerPane, FlowPane collFlow){
        collectionList.remove(card);
        addToOffer(offerGrid, card, flag);
        offerPane.setCenter(offerGrid);
        restoreCollection(null,flag,collFlow, collectionList);
    }

    private static EventHandler<MouseEvent> moveCardsOffer(FlowPane flow, BorderPane pane, boolean flag, Card card){
        EventHandler<MouseEvent> event = new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent mouseEvent) {
                if (mouseEvent.getButton().equals(MouseButton.PRIMARY)) {

                    if(flag) {

                        handleEventOffer(myCollectionList,card,flag, myCollFlow,flow,myImageList,myCardOffer,pane);

                    }
                    else{

                        handleEventOffer(otherCollectionList,card,flag,otherCollFlow,flow,otherImageList,otherCardOffer,pane);

                    }

                }
            }
        };

        return event;
    }

    private static void handleEventOffer(ArrayList<Card> collectionList, Card card, boolean flag, FlowPane collFlow, FlowPane flow, ArrayList<Card> imageList, Collection cardOffer, BorderPane pane){
        cardOffer.removeCardFromCollection(card);
        collFlow.getChildren().add(pane);
        restoreCollection(card, flag, collFlow, collectionList);
        imageList.remove(card);
        flow.getChildren().remove(pane);
    }
}