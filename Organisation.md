## Team Members:
* Hao Chen Li (lihaochen987)
* Nathan Laing (nukkienuk)
* Daena Govender (Daena-G)

## Summary of roles:

Initial discussions split the tasks up into three parts: the domain model (with associated endpoints), publish/subscribe functionalities and refactoring.

After some initial development, the team decided to employ pair-programming as the primary method of project completion. This was achieved either in-person, or with one team member sharing screens when completing the task.

For consistency, Hao was delegated to be the only member to push commits to the project. Any subsequents by Nathan were pushed when Hao was not available.

## Task completion responsibilities:
Overall the breakdown of task completion includes:
* Domain Model (and associated endpoints): Completed by Hao and Daena.
* Publish/subscribe pattern: Completed by Hao and Nathan.
* Refactoring: Completed by all team members.

## Domain Model:
The method the team took to developing the Domain Model was to copy all the DTOs and refactor the differing names. Variations from this decision are included below:

### Booking:
A bi-directional many-to-one relationship with User was specified in Booking. The reason for this was only allowing users to check their own bookings, and not of others. As a result, the Booking was persisted in the database with reference to the User who made that booking, and checked respectively. 

### Performer:
A bi-directional many-to-many relationship was established between Performer and Concert. In addition to the requirements from the brief, the bi-directional relationship was used primarily for increased readability among group members, and possible future implementation as it was determined by the team that Users were likely to be interested in the respective Concerts that the Performer performed at. 

### User:
A token field was provided to the user. This assisted in token-based authentication where everytime the User logged in, they would be given an authentication token associated with their account. Subsequent tasks requiring an authorisation check would utilise this authentication token.

### ConcertInfoNotifcation / ConcertInfoSubscription / BookingRequest
Not included in the domain model. The team determined no reason to persist them in the database. Each of the above classes fulfilled their role respectively, and there were no external calls from other classes to the classes listed above, they remained as DTOs.


## Strategy employed to minimise the chance of concurrency errors
