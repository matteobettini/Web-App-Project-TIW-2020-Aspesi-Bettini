/**
 * Bank
 */
(function(){
    //Vars
    var transferResult, userInfo, accountList, addressBook, transferList;
    
    var pageOrchestrator = new PageOrchestrator();

    window.addEventListener("load", () => {
        pageOrchestrator.start(); // initialize the components
        pageOrchestrator.refresh(); // display initial content
    });

    /**
     * Notes:
     * - excludeContacts parameter in PageOrchestrator.refresh(..) is used to avoid requesting
     *   address book to server when it's not needed (i.e. always but at first loading).
     *   Others refreshes will be managed by AddressBook object itself.
     */
    function PageOrchestrator(){
        this.start = function(){
            //Init components
            userInfo = new UserInfo(
                sessionStorage.getItem('name'), 
                sessionStorage.getItem('id'), 
                [document.getElementById("userName"), document.getElementById("headerUserName")], 
                [document.getElementById("headerUserCode")], 
                document.getElementById("logout-button")
            );

            accountList = new AccountList(
                document.getElementById("create-account-form"), 
                document.getElementById("account-form-button"), 
                document.getElementById("create-account-warning"), 
                document.getElementById("create-account-button"), 
                document.getElementById("accounts"), 
                document.getElementById("accounts-message")
            );
            
            transferResult = new TransferResult({
                "result_div" : document.getElementById("result-div"),
                "confirmed_div" : document.getElementById("confirmed-div"),
                "failed_div" : document.getElementById("failed-div"),
                "src_account_code_span" : document.getElementById("src-account-code"),
                "src_owner_code_span" : document.getElementById("src-owner-code"),
                "src_account_name_span" : document.getElementById("src-account-name"),
                "src_account_balance_span" : document.getElementById("src-account-balance"),
                "transfer_amount_span" : document.getElementById("transfer-amount"),
                "transfer_reason_span" : document.getElementById("transfer-reason"),
                "dest_account_code_span" : document.getElementById("dest-account-code"),
                "dest_owner_code_span" : document.getElementById("dest-owner-code"),
                "dest_account_name_span" : document.getElementById("dest-account-name"),
                "dest_account_balance_span" : document.getElementById("dest-account-balance"),
                "failed_reason_span" : document.getElementById("failed-reason"),
                "close_success_button" : document.getElementById("close-success"),
                "close_failed_button" : document.getElementById("close-failed")
            });

            transferList = new TransferList(
                document.getElementById("account-details"),
                document.getElementById("account-name"),
                document.getElementById("account-code"),
                document.getElementById("account-balance"),
                document.getElementById("create-transfer-form"),
                document.getElementById("transfer-form-button"),
                document.getElementById("create-transfer-button"),
                document.getElementById("transfers"),
                document.getElementById("transfers-message")
            );

            addressBook = new AddressBook(
                document.getElementById("add-contact"), 
                document.getElementById("dest-owner-code"), 
                document.getElementById("dest-account-code"), 
                document.getElementById("add-contact-warning"), 
                document.getElementById("add-contact-status-loading"), 
                document.getElementById("add-contact-status-ok"),
                document.getElementById("add-contact-status-ko"),
                document.getElementById("create-transfer-warning"),
                document.getElementById("dest-ids-datalist"),
                document.getElementById("dest-accounts-datalist")
            );
        };
        this.refresh = function(excludeContacts){
            //Refresh view
            userInfo.show();
            accountList.show();
            if(!excludeContacts)
                addressBook.load();
        };
    }

    function UserInfo(
        _name, 
        _usercode, 
        nameElements, 
        codeElements, 
        _logout_button){ 

        this.name = _name;
        this.code = _usercode;
        this.logout_button = _logout_button;

        this.logout_button.addEventListener("click", e => {
            sessionStorage.clear();
        });

        this.show = function(){
            nameElements.forEach(element => {
                element.textContent = this.name;
            });
            codeElements.forEach(element => {
                element.textContent = this.code;
            });
        }
    }

    /**
     * Notes:
     * - isNaN is used in combo with NaN, because of autoconversion of input parameter.
     *   (see https://developer.mozilla.org/it/docs/Web/JavaScript/Reference/Global_Objects/isNaN)
     * 
     * - List account_names could be retrived from document using attribute data_accountid,
     *   but we aimed at optimizing check rapidity
     * 
     * - When creating account, only the account list refreshes (and eventually autoclicks the account the user
     *  had already open, to keep its details open)
     */
    function AccountList(
        _create_account_form, 
        _account_form_button,
        _create_account_warning,
        _create_account_button,
        _accounts, 
        _accounts_message){

        this.create_account_form_div = _create_account_form;
        this.account_form_button = _account_form_button;
        this.create_account_warning = _create_account_warning;
        this.create_account_button = _create_account_button;
        this.accounts = _accounts;
        this.accounts_message = _accounts_message;

        this.currentSelectedId = NaN;
        this.last_used_open_button = null;
        this.account_names = [];

        var self = this; //Necessary only for in-function helpers (makeCall)
        //Link to buttons
        this.create_account_button.addEventListener('click', (e) =>{
            var button_label = e.target.textContent;
            if(button_label === 'Create account'){
                e.target.textContent = 'Hide form';
                self.create_account_warning.style.display = 'none';
                self.create_account_form_div.style.display='block';
            }else{
                e.target.textContent = 'Create account';
                self.create_account_form_div.style.display='none';
            }
        });

        this.account_form_button.addEventListener("click", (e) =>{

            self.create_account_warning.style.display = 'none';

            var create_account_form = e.target.closest("form");
            if(create_account_form.checkValidity()){
                
                var input_name = create_account_form.querySelector("input[name='accountName']");
                if(self.account_names.includes(input_name.value)){
                    create_account_form.reset();
                    self.create_account_warning.textContent = "Chosen account name already exists";
                    self.create_account_warning.style.display = 'block';
                    return;
                }

                makeCall("POST", 'CreateAccount', create_account_form, (req) =>{
                    switch(req.status){
                        case 200: //ok
                            var click = new Event("click");
                            self.create_account_button.dispatchEvent(click);
                            self.show();
                            break;
                        case 400: // bad request
                        case 401: // unauthorized
                        case 500: // server error
                            self.create_account_warning.textContent = req.responseText;
                            self.create_account_warning.style.display = 'block';
                            break;
                        default: //Error
                            self.create_account_warning.textContent = "Request reported status " + req.status;
                            self.create_account_warning.style.display = 'block';
                    }
                });
            }else{
                create_account_form.reportValidity();
            }
        });

        this.show = function(){
            //Request and update with the results
            makeCall("GET", 'GetAccounts', null, (req) =>{
                switch(req.status){
                    case 200: //ok
                        var accounts = JSON.parse(req.responseText);
                        self.update(accounts);
                        if(!isNaN(self.currentSelectedId)){
                            var open_account_button = document.querySelector("a[data_accountid='" + self.currentSelectedId + "']");
                            var click = new Event("click");
                            if(open_account_button)
                                open_account_button.dispatchEvent(click);
                        } 
                        break;
                    case 400: // bad request
                    case 401: // unauthorized
                    case 500: // server error
                        self.update(null, req.responseText);
                        break;
                    default: //Error
                        self.update(null, "Request reported status " + req.status);
                        break;
                }
            });
        };
        this.update = function(_accounts, _error) {
            
            self.accounts.style.display = "none";
            self.accounts.innerHTML = "";
            self.account_names.splice(0,self.account_names.length); //Clear array

            if(_error){

                self.accounts_message.textContent = _error;
                if(!self.accounts_message.className.includes("warning-message"))
                    self.accounts_message.className += " warning-message";
                self.accounts_message.style.display = "block";
                
            }else{

                if(_accounts.length === 0){
                    if(self.accounts_message.className.includes("warning-message"))
                        self.accounts_message.className.replace(" warning-message", "");
                    self.accounts_message.textContent = "You have no accounts in our bank :(";
                    self.accounts_message.style.display = "block";
                }else{
                    self.accounts_message.style.display = "none";
                    let card, card_title, card_data, b1, b2, br, open_button;
                    let i = 0;
                    _accounts.forEach((acc) => {
                        card = document.createElement("div");
                        card.className = "card card-blue";
                        if( i % 2 === 0)
                            card.className += " even";
                        card_title = document.createElement("div");
                        card_title.className = "card-title";
                        card_title.textContent = acc.name;
                        card.appendChild(card_title);
                        card_data = document.createElement("div");
                        card_data.className = "card-data";

                        b1 = document.createElement("b");
                        b1.textContent = "Code: ";
                        card_data.appendChild(b1);
                        card_data.appendChild(document.createTextNode(acc.id));
                    
                        br = document.createElement("br");
                        card_data.appendChild(br);

                        b2 = document.createElement("b");
                        b2.textContent = "Balance: ";
                        card_data.appendChild(b2);
                        card_data.appendChild(document.createTextNode(acc.balance + "\u20AC"));
                

                        card.appendChild(card_data);
                        open_button = document.createElement("a");
                        open_button.className = "btn btn-purple btn-small btn-primary";
                        open_button.textContent = "Open";
                        open_button.setAttribute('data_accountid', acc.id);
                        open_button.addEventListener("click", (e) => {
                            if(e.target.textContent === "Open"){
                                if(self.last_used_open_button !== null){
                                    self.last_used_open_button.textContent = "Open";
                                }
                                e.target.textContent = "Hide";
                                self.last_used_open_button = e.target;
                                self.currentSelectedId = acc.id;
                                transferList.show(acc.id);
                            }else{
                                self.last_used_open_button = null;
                                self.currentSelectedId = NaN;
                                e.target.textContent = "Open";
                                transferList.hide();
                            }
                        });
                        card.appendChild(open_button);
                        self.account_names.push(acc.name);
                        self.accounts.appendChild(card);
                        i++;
                    });
                    self.accounts.style.display = "block";
                }
            }
        };
    }

    /**
     * Notes:
     * - focus event is used to update (and display) suggestions before the user starts typing.
     * - keyup event is used because we needed to know the updated content of the input, after
     *   character is added to the input.
     *   (see https://www.w3.org/TR/DOM-Level-3-Events/#events-keyboard-event-order)
     */
    function TransferList(
                        _account_details,
                        _account_name, 
                        _account_code,
                        _account_balance, 
                        _create_transfer_form_div, 
                        _transfer_form_button,
                        _create_transfer_button,
                        _transfers, 
                        _transfers_message){
        //Saving vars
        this.account_details = _account_details;
        this.account_name_span = _account_name;
        this.account_code_span = _account_code;
        this.account_balance_span = _account_balance;
        this.create_transfer_form_div = _create_transfer_form_div;
        this.transfer_form_button = _transfer_form_button;
        this.create_transfer_button = _create_transfer_button;
        this.transfers = _transfers;
        this.transfers_message = _transfers_message;
        
        this.create_transfer_form = this.create_transfer_form_div.querySelector("form");
        this.dest_input = this.create_transfer_form.querySelector("input[name='destUserId']");
        this.account_input = this.create_transfer_form.querySelector("input[name='destAccountId']");
        this.amount_input = this.create_transfer_form.querySelector("input[name='amount']");
        this.source_id = this.create_transfer_form.querySelector("input[name='sourceAccountId']");
        
        //Attach listeners
        this.create_transfer_button.addEventListener("click", (e) => {
            if(this.create_transfer_button.textContent === 'Create transfer'){
                this.showCreate();
            }else{
                this.hideCreate(false); //Avoid resetting form
            }
        });
        this.transfer_form_button.addEventListener("click", (e) =>{
            //Get form
            if(this.create_transfer_form.checkValidity()){
                //Check account loop
                if (this.account_input.value == this.source_id.value){
                    this.create_transfer_form.reset();
                    transferResult.showFailure("Cannot make transfers on the same account");
                    return;
                }else if (Number(this.amount_input.value) > Number(this.account_balance_span.textContent)){
                    this.create_transfer_form.reset();
                    transferResult.showFailure("You don't have enough money on this account to make this transfer");
                    return;
                }
                //Make request
                var self = this;
                makeCall("POST", 'MakeTransfer', this.create_transfer_form, (req) =>{
                    switch(req.status){
                        case 200: //ok
                            var data = JSON.parse(req.responseText);
                            pageOrchestrator.refresh(true);
                            //Close form
                            var click = new Event("click");
                            self.create_transfer_button.dispatchEvent(click);

                            transferResult.showSuccess(data.sourceAccount, data.transfer, data.destAccount);
                            break;
                        case 400: // bad request
                        case 401: // unauthorized
                        case 500: // server error
                            transferResult.showFailure(req.responseText);
                            break;
                        default: //Error
                            transferResult.showFailure("Request reported status " + req.status);
                    }
                });
            }else{
                this.create_transfer_form.reportValidity();
            }
        });
        this.dest_input.addEventListener("focus", e => {
            addressBook.autocompleteDest(e.target.value);
        });
        this.dest_input.addEventListener("keyup", e => {
            addressBook.autocompleteDest(e.target.value);  
        });
        this.account_input.addEventListener("focus", e => {
            addressBook.autocompleteAccount(this.dest_input.value, e.target.value, this.source_id.value);
        });
        this.account_input.addEventListener("keyup", e => {
            addressBook.autocompleteAccount(this.dest_input.value, e.target.value, this.source_id.value);
        });
        this.showCreate = function(){
            this.create_transfer_button.textContent = 'Hide form';
            this.create_transfer_form_div.style.display = 'block';
        };
        this.hideCreate = function(reset){
            this.create_transfer_button.textContent = 'Create transfer';
            this.create_transfer_form_div.style.display = 'none'; 
            if (reset)
                this.create_transfer_form.reset();
        };
        this.show = function(accountID){
            //Request and update with the results
            var self = this;
            makeCall("GET", 'GetAccountDetails?accountId=' + accountID, null, (req) =>{
                switch(req.status){
                    case 200: //ok
                        var data = JSON.parse(req.responseText);
                        self.update(data.account, data.transfers, false);
                        break;
                    case 400: // bad request
                    case 401: // unauthorized
                    case 500: // server error
                        self.update(null, req.responseText);
                        break;
                    default: //Error
                        self.update(null, "Request reported status " + req.status);
                        break;
                }
            });
        };
        this.hide = function(){
            this.account_details.style.display = "none";
            this.hideCreate(true); //Also reset form
        };
        this.update = function(account, transfers, error_message){
            //Hide content while refreshing
            this.hide();
            //Init headers
            this.account_name_span.textContent = account.name;
            this.account_code_span.textContent = account.id;
            this.account_balance_span.textContent = account.balance;
            //Init message
            this.transfers_message.className = (error_message ? "warning-message" : "");
            this.transfers_message.style.display = (error_message || transfers.length === 0 ? "block" : "none");// invert order
            //Init hidden data
            this.source_id.value = account.id;

            //Clear content
            this.transfers.innerHTML = "";
            if (error_message){
                this.transfers_message.textContent = error_message;
                this.account_details.style.display = "block";
                return;
            }else if (transfers.length === 0){
                this.transfers_message.textContent = "You have no transfers for this account :(";
                this.account_details.style.display = "block";
                return;
            }
            
            //Init list   
            transfers.forEach((transfer) => {

                let card, card_title, card_data, b1, br, b2, amount_div;

                card = document.createElement("div");
                card.className = "linked-card linked-card-blue";

                card_title = document.createElement("div");
                card_title.className = "linked-card-title";
                card_title.textContent = (transfer.sourceAccountID === account.id ? "Destination Account: " + transfer.destinationAccountID : "Source Account: " + transfer.sourceAccountID);
                card.appendChild(card_title);
                
                card_data = document.createElement("div");
                card_data.className = "linked-card-data";

                b1 = document.createElement("b");
                b1.textContent = "Timestamp: ";
                card_data.appendChild(b1);
                card_data.appendChild(document.createTextNode(transfer.timestamp));
                    
                br = document.createElement("br");
                card_data.appendChild(br);

                b2 = document.createElement("b");
                b2.textContent = "Reason: ";
                card_data.appendChild(b2);
                card_data.appendChild(document.createTextNode(transfer.reason));

                amount_div = document.createElement("div");
                amount_div.className = "transfers-amount " + (transfer.sourceAccountID === account.id ? "negative" : "positive");
                amount_div.appendChild(document.createTextNode((transfer.sourceAccountID === account.id ? "-" : "+" ) + transfer.amount + "\u20AC"));
                card_data.appendChild(amount_div);
                
                card.appendChild(card_data);

                this.transfers.appendChild(card);
            });
            this.account_details.style.display = "block";
        };
    }

    function TransferResult(options){
        //Save html elements in scope
        this.result_div = options["result_div"];
        this.confirmed_div = options["confirmed_div"];
        this.failed_div = options["failed_div"];
        this.src_account_code_span = options["src_account_code_span"];
        this.src_owner_code_span = options["src_owner_code_span"];
        this.src_account_name_span = options["src_account_name_span"];
        this.src_account_balance_span = options["src_account_balance_span"];
        this.transfer_amount_span = options["transfer_amount_span"];
        this.transfer_reason_span = options["transfer_reason_span"];
        this.dest_account_code_span = options["dest_account_code_span"];
        this.dest_owner_code_span = options["dest_owner_code_span"];
        this.dest_account_name_span = options["dest_account_name_span"];
        this.dest_account_balance_span = options["dest_account_balance_span"];
        this.failed_reason_span = options["failed_reason_span"];
        this.close_success_button = options["close_success_button"];
        this.close_failed_button = options["close_failed_button"];

        //Setup listeners
        this.close_success_button.addEventListener("click", e => {
            this.result_div.style.display = 'none';
        });
        this.close_failed_button.addEventListener("click", e => {
            this.result_div.style.display = 'none';
        });

        this.showSuccess = function(srcAccount, transfer, destAccount){
            //Update spans
            this.src_account_code_span.textContent = srcAccount.id;
            this.src_owner_code_span.textContent = srcAccount.userId;
            this.src_account_name_span.textContent = srcAccount.name;
            this.src_account_balance_span.textContent = srcAccount.balance;
            this.transfer_amount_span.textContent = transfer.amount;
            this.transfer_reason_span.textContent = transfer.reason;
            this.dest_account_code_span.textContent = destAccount.id;
            this.dest_owner_code_span.textContent = destAccount.userId;
            this.dest_account_name_span.textContent = destAccount.name;
            this.dest_account_balance_span.textContent = destAccount.balance;
            //Setup visibility
            addressBook.showButton(destAccount.userId, destAccount.id);
            this.show(true);
        };
        this.showFailure = function(reason){
            //Update spans
            this.failed_reason_span.textContent = reason;
            //Setup visibility
            this.show(false);
        }
        this.show = function(displaySuccess){
            this.confirmed_div.style.display = (displaySuccess ? 'block' : 'none');
            this.failed_div.style.display = (displaySuccess ? 'none' : 'block');
            this.result_div.style.display = 'block';
        }
    }

    /**
     * Notes:
     * - Multiple images for displaying contact adding outcome are used for
     *   optimizing first loading. Setting them only in css, would have caused a late
     *   loading when not already in cache.
     * 
     * - the address book is stored as a "map" inside contacts variable. Actually, is parsed
     *   from json as an Object, with properties destIDs, each with an associated List of destAccounts.
     * 
     * - destIDs are converted into a List with Object.keys(), as we needed to do also partial matching.
     *   Because destIDs is a List of String (property names), when actually elements are numbers, 
     *   and the inputDestID.value is a number, we needed an Array function which would compare elements 
     *   with autocasting.
     *   This is why we introduced method Array.contains(el):boolean (utils.js)
     */
    function AddressBook(
        _add_contact, 
        _destination_user_span,
        _destination_account_span, 
        _add_contact_warning_div, 
        _add_contact_status_loading,  
        _add_contact_status_ok,  
        _add_contact_status_ko,
        _create_transfer_warning,
        _dest_ids_datalist,
        _dest_accounts_datalist){

        this.add_contact = _add_contact;
        this.destination_account_span = _destination_account_span;
        this.destination_user_span = _destination_user_span;
        this.add_contact_warning_div = _add_contact_warning_div;
        this.add_contact_status_loading = _add_contact_status_loading;
        this.add_contact_status_ok = _add_contact_status_ok;
        this.add_contact_status_ko = _add_contact_status_ko;
        this.create_transfer_warning = _create_transfer_warning;
        this.dest_ids_datalist = _dest_ids_datalist;
        this.dest_accounts_datalist = _dest_accounts_datalist;
        this.contacts = [];
        
        var self = this;

        this.add_contact.addEventListener("click", (e) => {
            e.target.style.display = "none";
            self.add_contact_status_loading.style.display = "block";

            var destUsrId = self.destination_user_span.textContent;
            var destAccId = self.destination_account_span.textContent;
            self.addContact(destUsrId, destAccId);
        });

        this.load = function(){
            makeCall("GET", "GetContacts", null, (req) => {
                switch(req.status){
                    case 200: //ok
                        self.create_transfer_warning.style.display = "none";
                        self.contacts = JSON.parse(req.responseText);
                        break;
                    case 400: // bad request
                    case 401: // unauthorized
                    case 500: // server error
                    default: //Error
                        self.create_transfer_warning.textContent = "Unable to load your contacts";
                        self.create_transfer_warning.style.display = "block";
                        break;
                }
            });
        };
        this.showButton = function(destUserCode, destAccountCode){
            
            self.add_contact_warning_div.style.display = "none";
            self.add_contact_status_loading.style.display = "none";
            self.add_contact_status_ok.style.display = "none";
            self.add_contact_status_ko.style.display = "none";

            if(self.contacts[destUserCode]){
                if(self.contacts[destUserCode].includes(destAccountCode)){
                    self.add_contact.style.display = "none";
                    self.add_contact_status_ok.style.display = "block";
                    return;
                }
            }
            
            self.add_contact.style.display = "block";
        };

        this.addContact = function(destUserCode, destAccountCode){
            //Create form data
            var data = new FormData();
            data.append("contactId", destUserCode);
            data.append("contactAccountId", destAccountCode);
            //Send data
            makeCall("POST", "AddContact", data, (req) => {
                switch(req.status){
                    case 200: //ok
                        self.load();
                        self.add_contact_status_loading.style.display = "none";
                        self.add_contact_status_ok.style.display = "block";
                        break;
                    case 400: // bad request
                    case 401: // unauthorized
                    case 500: // server error
                        self.add_contact_status_loading.style.display = "none"
                        self.add_contact_warning_div.textContent = req.responseText;
                        self.add_contact_status_ko.style.display = "block";
                        self.add_contact_warning_div.style.display = "block";
                        break;
                    default: //Error
                        self.add_contact_status_loading.style.display = "none";
                        self.add_contact_warning_div.textContent = "Request reported status " + req.status;
                        self.add_contact_status_ko.style.display = "block";
                        self.add_contact_warning_div.style.display = 'block';
                        break;
                }
            });
        };

        this.autocompleteDest = function(dest_id){
            //Clear suggestions
            this.dest_ids_datalist.innerHTML = "";
            this.dest_accounts_datalist.innerHTML = "";
            //Get dest match
            var destIDs = Object.keys(this.contacts);
            if (!destIDs.contains(dest_id)){ //If not already matched
                //Load partial suggestions
                let similarIDs = [];
                destIDs.forEach(dest => {
                    if (String(dest).startsWith(dest_id)) //If matches start
                        similarIDs.push(dest); //Add to suggested
                });
                similarIDs.forEach(dest => {
                    let option = document.createElement("option");
                    option.text = dest;
                    option.value = dest;
                    this.dest_ids_datalist.appendChild(option);
                });
            }
        };
        this.autocompleteAccount = function(dest_id, account_id, current_account){
            //Clear suggestions
            this.dest_ids_datalist.innerHTML = "";
            this.dest_accounts_datalist.innerHTML = "";
            //Get dest match
            var destIDs = Object.keys(this.contacts);
            if (destIDs.contains(dest_id)){ //If already matched
                //Dest is already okay, suggest his accounts
                let accountIDs = this.contacts[dest_id];
                if (!accountIDs.contains(account_id)){ //If not already matched
                    let similarIDs = [];
                    accountIDs.forEach(account => {
                        if (String(account).startsWith(account_id) && account != current_account) //Similar, but not this account
                            similarIDs.push(account);
                    });
                    similarIDs.forEach(account => {
                        let option = document.createElement("option");
                        option.text = account;
                        option.value = account;
                        this.dest_accounts_datalist.appendChild(option);
                    });
                }
            }
        }
    }

})();