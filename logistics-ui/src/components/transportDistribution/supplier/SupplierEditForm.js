import * as React from 'react';
import Button from '@material-ui/core/Button';
import {Field, Form, Formik} from 'formik';
import {TextField} from 'formik-material-ui';
import Dashboard from "../../../pages/transportDistribution/Dashboard";
import Grid from "@material-ui/core/Grid/Grid";
import {Supplier} from "../../../types";
import withStyles from "@material-ui/core/styles/withStyles";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import type {Values} from "./SupplierAddForm";
import update from "immutability-helper";

type Props = {
    supplier: Supplier,
    dashboardRef: Dashboard
}

export function SupplierEditForm(props: Props) {
    return <Formik
        initialValues={{
            name: props.supplier.name,
            supply: props.supplier.supply,
            available: props.supplier.available || '',
            priority: props.supplier.priority ||'',
            limit: props.supplier.limit || '',
            purchaseCost: props.supplier.purchaseCost || ''
        }}
validate={values => {
    const errors: Partial<Values> = {};
    if (!values.name) {
        errors.name = 'Required';
    }
    if (!values.supply) {
        errors.supply = 'Required'
    }else{
        if(values.supply <= 0){
            errors.supply = 'Supply must be positive value'
        }
    }
    return errors;
}}
onSubmit={(values, {setSubmitting}) => {
    setSubmitting(false);
    props.dashboardRef.handleUpdateSupplier(
        update(props.supplier, {
            name: {$set: values.name || props.supplier.name},
            available: {$set: Math.min(values.available || props.supplier.available, values.supply || props.supplier.supply)},
            priority: {$set: values.priority || props.supplier.priority},
            limit: {$set: values.limit || props.supplier.limit},
            supply: {$set: values.supply || props.supplier.supply},
            purchaseCost: {$set: values.purchaseCost || props.supplier.purchaseCost}
        }))
}}
render={({submitForm, isSubmitting}) => (
    <Form>
        <Grid
            container
            justify="center"
            alignItems="center"
            spacing={16}
        >
            <Grid item xs>
                <Field
                    name="name"
                    type="text"
                    label="Supplier name"
                    component={TextField}
                />
            </Grid>
            <Grid item xs>
                <Field
                    name="supply"
                    type="number"
                    label="Maximal supply"
                    component={TextField}
                />
            </Grid>
            <Grid item xs>
                <Field
                    name="available"
                    type="number"
                    label="Currently available"
                    component={TextField}
                />
            </Grid>
            <Grid item xs>
                <Field
                    name="limit"
                    type="number"
                    label="Disposal limit"
                    component={TextField}
                />
            </Grid>
            <Grid item xs>
                <Field
                    name="priority"
                    type="number"
                    label="Supplier priority"
                    component={TextField}
                />

            </Grid>
            <Field
                name="purchaseCost"
                type="number"
                label="Unit purchase cost"
                component={TextField}
            />
            <Grid item xs>
                <Button
                    variant="contained"
                    color="primary"
                    disabled={isSubmitting}
                    onClick={submitForm}
                >
                    <FontAwesomeIcon icon={"save"}/>
                </Button>
            </Grid>
        </Grid>
    </Form>
)}
/>
}

const styles = theme => ({
    root:{
        padding: theme.spacing.unit * 2
    }
});


export default withStyles(styles)(SupplierEditForm)

