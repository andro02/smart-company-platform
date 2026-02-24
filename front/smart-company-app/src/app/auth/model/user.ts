export enum UserType {
    Manager = "Manager",
    Guest = "Guest",
    Customer = "Customer",
    SuperAdmin = "SuperAdmin",
}

export class User {

    public username: string;
    public password: string;
    public type: UserType;
    public isActive: boolean;
    public email: string;
    public image: string;

    constructor(
        username: string,
        password: string,
        type: UserType,
        isActive: boolean,
        email: string,
        image: string
    ) {
        this.username = username;
        this.password = password;
        this.type = type;
        this.isActive = isActive;
        this.email = email;
        this.image = image;
    }

}

export const UserTypeMapping: Record<UserType, string> = {
    [UserType.Manager]: "Manager",
    [UserType.Guest]: "Guest",
    [UserType.Customer]: "Customer",
    [UserType.SuperAdmin]: "SuperAdmin",
}