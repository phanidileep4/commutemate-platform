# Domain model

Organization 1---* Location
Organization 1---* Membership *---1 User
User 1---1 MemberProfile
MemberProfile 1---* CommutePreference
User 1---* RideOffer
User 1---* RideRequest
RideOffer/Request -> MatchProposal -> Ride
Ride -> RideParticipant
Ride -> Feedback
Organization -> MatchingPolicy
Organization -> ParkingFacility -> ParkingSpace/CapacityBucket

Important: `User` is platform identity; `Membership` is tenant identity. A person may belong to more than one tenant without cross-tenant data leakage.
