/**
 * Created by nagesingh on 10/9/2020.
 */

trigger ContactTrigger on Contact (after insert, after update, after delete, after undelete) {

    Map<Id, Contact> contactMap = Trigger.newMap;
    Map<Id, Contact> contactOldMap = Trigger.oldMap;
    if(Trigger.isInsert && Trigger.isAfter){

        ContactTriggerHandler.summationOnAccount(contactMap, false);
    }

    if(Trigger.isUpdate && Trigger.isAfter){

        ContactTriggerHandler.summationOnAccount(contactMap, false);
    }

    if(Trigger.isAfter && Trigger.isDelete){

        ContactTriggerHandler.summationOnAccount(contactOldMap, true);
    }

    if(Trigger.isAfter && Trigger.isUndelete){

        ContactTriggerHandler.summationOnAccount(contactMap, false);
    }
}
