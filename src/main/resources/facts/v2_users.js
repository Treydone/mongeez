//mongeez formatted javascript
//changeset other:v1_2
db.users.update(
    {
        entity : 'SVABIGDATA'
    },
    {
         $inc : {
             version : 1
         },
         $set : {
             property2 : 666
         }
    },
    {
        multi: true
    }
);