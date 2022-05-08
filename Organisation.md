[## Team Members:
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
A bi-directional many-to-one relationship with User was specified in Booking. The reason for this was only allowing users to check their own bookings, and not of others. As a result, the Booking was persisted in the database with reference to the User who made that booking, and checked respectively. As there is only one User, and the contents of that User is likely to be accessed, an EAGER fetching strategy was utilised.

An EAGER fetching type was used on the bi-directional relationship between Booking and Seat. All queries to the existing array of Seats require an iteration through all Seat objects for them to be modified. As a result, loading all Seats at once would save resources.

### Concert:
The uni-directional relationship between Concert and Date utilises a LAZY fetch type. The Date is retrieved from the Concert by the User to view the available bookings. Rarely is there an instance where all Dates need to be retrieved, hence a LAZY fetch type is used.  

This bi-directional many-to-many relationship between Concert and Performer utilises EAGER fetching. The reasoning behind this is because when viewing the Performers in a concert, we are often interested in all of them rather than just one. 


### Performer:
A bi-directional many-to-many relationship was established between Performer and Concert. This relationship instead utilises LAZY fetching. As the application focuses on booking Concerts, it is likely that the User would be interested in only one of the Concerts the Performer has performed in. Loading all information for all Concerts for the Performer would likely be a waste of resources.

### User:
A token field was provided to the user. This assisted in token-based authentication where everytime the User logged in, they would be given an authentication token associated with their account. Subsequent tasks requiring an authorisation check would utilise this authentication token.

### ConcertInfoNotifcation / ConcertInfoSubscription / BookingRequest
Not included in the domain model. The team determined no reason to persist them in the database. Each of the above classes fulfilled their role respectively, and there were no external calls from other classes to the classes listed above, they remained as DTOs.


## Strategy employed to minimise the chance of concurrency errors:
The two main classes identified were User and Seat to have concurrency issues were User and Seat.

### User
A problem would occur when two users would Login to the same account at the same time. Although highly unlikely, an Optimistic Concurrency Control technique would check which version of the User was committed first.

### Seat
Examples of concurrency issues would be double-booking seats, and when two Users book overlapping seats at the same time. The team did not implement Optimistic Concurrency Control for the Booking class directly. As Booking would immediately Book seats for the appropriate user, a failure in Seat would prevent the Booking from completing at all.

### Preventions
Optimistic Concurrency Control was used as the team did not expect the above concurrency issues to occur frequently. The likelihood of overlapping seat bookings at the same time, or when Users login to the same account at the same time is highly unlikely. As a result, Pessimistic Locking was not utilised and the alternative was considered instead.

Additional conditionals were utilised to guarantee the concurrency of the application.
