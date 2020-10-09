/**
 * Created by nagesingh on 10/9/2020.
 */

public class ContactTriggerHandler {

    public static void summationOnAccount(Map<Id, Contact> contactMap, Boolean isDelete){

        Map<Id, List<Contact>> mapOfAccountAndContacts = new Map<Id, List<Contact>>();
        for(Id contactId : contactMap.keySet()){
            Contact contact = contactMap.get(contactId);
            if(contact.AccountId != null && mapOfAccountAndContacts.containsKey(contact.AccountId)){
                List<Contact> contacts = mapOfAccountAndContacts.get(contact.AccountId);
                contacts.add(contact);
            } else {
                List<Contact> lstContacts = new List<Contact>();
                lstContacts.add(contact);
                if(contact.AccountId != null) {
                    mapOfAccountAndContacts.put(contact.AccountId, lstContacts);
                }
            }
        }

        List<Contact> contacts = [SELECT Id, Salary__c, AccountId FROM Contact WHERE AccountId IN :mapOfAccountAndContacts.keySet() AND Id NOT IN: contactMap.keySet()];

        for(Contact objEachContact : contacts){
            if(objEachContact.AccountId != null && mapOfAccountAndContacts.containsKey(objEachContact.AccountId)){
                List<Contact> lstContacts = mapOfAccountAndContacts.get(objEachContact.AccountId);
                if(!lstContacts.contains(objEachContact)) {
                    lstContacts.add(objEachContact);
                }
            }
        }

        List<Account> lstTooBeUpdated = new List<Account>();
        for(Id accountId : mapOfAccountAndContacts.keySet()){
            List<Contact> finalContacts = mapOfAccountAndContacts.get(accountId);
            Integer countSalary = 0;
            for (Contact eachContact : finalContacts){
                if(isDelete && contactMap.containsKey(eachContact.Id)){
                    continue;
                }
                if(eachContact.Salary__c != null){
                    countSalary = countSalary + Integer.valueOf(eachContact.Salary__c);
                } else if(contactMap.containsKey(eachContact.Id) && contactMap.get(eachContact.Id).Salary__c != null){
                    countSalary = countSalary + Integer.valueOf(contactMap.get(eachContact.Id).Salary__c);
                }
            }

            Double dbl = Math.pow(countSalary, 3);
            lstTooBeUpdated.add(new Account(Id = accountId, TotalSalary__c = Integer.valueOf(dbl)));

        }

        Database.update(lstTooBeUpdated);

    }

}
