package test

class TestUser {
    String username
    String salt

    static constraints = {
        username nullable: false
        salt nullable: true
    }
}